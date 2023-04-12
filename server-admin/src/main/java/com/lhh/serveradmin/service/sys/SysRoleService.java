package com.lhh.serveradmin.service.sys;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serveradmin.feign.sys.SysRoleFeign;
import com.lhh.serveradmin.feign.sys.SysRoleMenuFeign;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.SysRoleEntity;
import com.lhh.serverbase.entity.SysRoleMenuEntity;
import com.lhh.serverbase.entity.SysUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleService {

    @Autowired
    SysRoleFeign sysRoleFeign;
    @Autowired
    SysRoleMenuFeign sysRoleMenuFeign;

    public IPage<SysRoleEntity> page(Map<String, Object> params){
        IPage<SysRoleEntity> list = sysRoleFeign.page(params);
        return list;
    }

    public List<SysRoleEntity> list(Map<String, Object> params){
        List<SysRoleEntity> list = sysRoleFeign.list(params);
        return list;
    }

    public SysRoleEntity info(Long id) {
        SysRoleEntity role = sysRoleFeign.info(id);
        List<SysRoleMenuEntity> roleMenuList = sysRoleMenuFeign.list(new HashMap<String, Object>(){{put("roleId", id);}});
        List<Long> menuIdList = roleMenuList.stream().map(SysRoleMenuEntity::getMenuId).collect(Collectors.toList());
        role.setMenuIdList(menuIdList);
        return role;
    }

    public void save(SysRoleEntity role){
        sysRoleFeign.save(role);
    }

    public void update(SysRoleEntity role) {
        sysRoleFeign.update(role);
        sysRoleMenuFeign.delByRoleId(role.getRoleId());
        List<SysRoleMenuEntity> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(role.getMenuIdList())) {
            for (Long menuId : role.getMenuIdList()) {
                SysRoleMenuEntity roleMenu = SysRoleMenuEntity.builder()
                        .roleId(role.getRoleId()).menuId(menuId)
                        .build();
                list.add(roleMenu);
            }
        }
        sysRoleMenuFeign.saveBatch(list);
    }

    public void deleteBatch(Long[] ids){
        sysRoleFeign.deleteBatch(ids);
    }

}
