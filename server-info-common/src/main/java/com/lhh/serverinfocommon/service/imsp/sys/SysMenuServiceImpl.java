package com.lhh.serverinfocommon.service.imsp.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.SysMenuEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.sys.SysMenuDao;
import com.lhh.serverinfocommon.service.sys.SysMenuService;
import com.lhh.serverinfocommon.service.sys.SysUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service("sysMenuService")
public class SysMenuServiceImpl extends ServiceImpl<SysMenuDao, SysMenuEntity> implements SysMenuService {

    @Value(value = "${custom-config.super-admin-id}")
    private String superAdminId;

    @Autowired
    private SysMenuDao sysMenuDao;
    @Autowired
    private SysUserService sysUserService;

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public IPage<SysMenuEntity> page(Map<String, Object> params) {
        IPage<SysMenuEntity> page = this.page(
                new Query<SysMenuEntity>().getPage(params),
                new QueryWrapper<SysMenuEntity>()
                        .eq("del_flg", Const.INTEGER_0)
        );
        return page;
    }

    /**
     * 查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public List<SysMenuEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query()
                .eq(params.get("type") != null, "type", params.get("type"))
                .eq("del_flg", Const.INTEGER_0);
        List<SysMenuEntity> list = list(wrapper);
        return list;
    }

    /**
     * 根据用户Id查询用户权限
     *
     * @param userId
     * @return
     */
    @Override
    public List<String> queryAllPerms(Long userId) {
        List<String> rst;
        if (superAdminId.equals(String.valueOf(userId))) {
            rst = sysMenuDao.queryAllPerms();
        } else {
            rst = sysMenuDao.queryAllPermsByUserId(userId);
        }
        List<String> permsSet = new ArrayList<>();
        if (rst != null && rst.size() > 0) {
            for (String perms : rst) {
                if (StringUtils.isBlank(perms)) {
                    continue;
                }
                permsSet.addAll(Arrays.asList(perms.trim().split(",")));
            }
        }
        return permsSet;
    }

    @Override
    public List<SysMenuEntity> queryUserMenuList(Long userId) {
        //系统管理员，拥有最高权限
        if (superAdminId.equals(String.valueOf(userId))) {
            return getAllMenuList(null);
        }

        //用户菜单列表
        List<Long> menuIdList = sysUserService.queryAllMenuId(userId);
        return getAllMenuList(menuIdList);
    }

    @Override
    public List<SysMenuEntity> queryListParentId(Long parentId, List<Long> menuIdList) {
        List<SysMenuEntity> menuList = queryListParentId(parentId);
        if (menuIdList == null) {
            return menuList;
        }

        List<SysMenuEntity> userMenuList = new ArrayList<>();
        for (SysMenuEntity menu : menuList) {
            if (menuIdList.contains(menu.getMenuId())) {
                userMenuList.add(menu);
            }
        }
        return userMenuList;
    }

    @Override
    public List<SysMenuEntity> queryListParentId(Long parentId) {
        return sysMenuDao.queryListParentId(parentId);
    }

    /**
     * 获取所有菜单列表
     */
    private List<SysMenuEntity> getAllMenuList(List<Long> menuIdList) {
        //查询根菜单列表
        List<SysMenuEntity> menuList = queryListParentId(0L, menuIdList);
        //递归获取子菜单
        getMenuTreeList(menuList, menuIdList);

        return menuList;
    }

    /**
     * 递归
     */
    private List<SysMenuEntity> getMenuTreeList(List<SysMenuEntity> menuList, List<Long> menuIdList) {
        List<SysMenuEntity> subMenuList = new ArrayList<SysMenuEntity>();

        for (SysMenuEntity entity : menuList) {
            //目录
            if (entity.getType() == Const.MenuType.CATALOG.getValue()) {
                entity.setList(getMenuTreeList(queryListParentId(entity.getMenuId(), menuIdList), menuIdList));
            }
            subMenuList.add(entity);
        }
        return subMenuList;
    }

    @Override
    public List<SysMenuEntity> findAll(Long id) {
        try {
            //查询出所有的菜单
            List<SysMenuEntity> allMenu = sysMenuDao.queryAllList();

            //根节点存储
            List<SysMenuEntity> rootMenu = new ArrayList<>();
            //根据传递的参数设置根节点
            if(id!= null){
                //父节点为传递的id为根节点
                for (SysMenuEntity nav : allMenu) {
                    if(nav.getParentId().equals(id)){
                        rootMenu.add(nav);
                    }
                }
            }else {
                //父节点是0的，为根节点
                for (SysMenuEntity nav : allMenu) {
                    if (nav.getParentId().equals(Const.LONG_0)) {
                        rootMenu.add(nav);
                    }
                }
            }

            //为根节点设置子菜单，getChild是递归调用
            for (SysMenuEntity nv : rootMenu) {
                //获取根节点下的所有子节点，使用getChild方法
                List<SysMenuEntity> childList = getChild(nv.getMenuId(), allMenu);
                //给根节点设置子节点
                nv.setChildren(childList);
            }

            return rootMenu;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    @Override
    public Set<Long> getChild(Long parentId) {
        List<SysMenuEntity> menuList = baseMapper
                .selectList(Wrappers.<SysMenuEntity>lambdaQuery().eq(SysMenuEntity::getParentId, parentId)
                        .orderByAsc(SysMenuEntity::getOrderNum));
        Set<Long> childList = menuList.stream().map(a -> a.getMenuId()).collect(Collectors.toSet());
        List<SysMenuEntity> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(childList)) {
            for (Long id : childList) {
                list.addAll(findAll(id));
            }
            list.addAll(menuList);
        }
        childList = list.stream().map(a -> a.getMenuId()).collect(Collectors.toSet());
        childList.add(parentId);
        return childList;
    }

    private List<SysMenuEntity> getChild(Long id, List<SysMenuEntity> allMenu) {
        //子菜单
        List<SysMenuEntity> childList = new ArrayList<>();
        for (SysMenuEntity nav : allMenu) {
            //遍历所有节点，将所有菜单的父id与传过来的根节点的id比较
            //相等说明：为该根节点的子节点
            if (nav.getParentId().equals(id)) {
                childList.add(nav);
            }
        }
        //递归设置子节点
        for (SysMenuEntity nav : childList) {
            nav.setChildren(getChild(nav.getMenuId(), allMenu));
        }
        //排序，，如果不需要排序，可直接注释掉
        childList = childList.stream().sorted(Comparator.comparing(SysMenuEntity::getOrderNum))
                .collect(Collectors.toList());
        //如果节点下没有子节点，返回一个空List（递归退出）
        if (childList.size() == 0) {
            return new ArrayList<SysMenuEntity>();
        }
        return childList;
    }

}
