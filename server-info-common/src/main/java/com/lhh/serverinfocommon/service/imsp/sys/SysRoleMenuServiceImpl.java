package com.lhh.serverinfocommon.service.imsp.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.SysRoleMenuEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.sys.SysRoleMenuDao;
import com.lhh.serverinfocommon.service.sys.SysRoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("sysRoleMenuService")
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuDao, SysRoleMenuEntity> implements SysRoleMenuService {

    @Autowired
    private SysRoleMenuDao sysRoleMenuDao;

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public IPage<SysRoleMenuEntity> page(Map<String, Object> params) {
        IPage<SysRoleMenuEntity> page = this.page(
                new Query<SysRoleMenuEntity>().getPage(params),
                new QueryWrapper<SysRoleMenuEntity>()
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
    public List<SysRoleMenuEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query()
                .eq(params.get("roleId") != null, "role_id", params.get("roleId"));
        List<SysRoleMenuEntity> list = list(wrapper);
        return list;
    }

    @Override
    public void delByRoleId(Long roleId) {
        if (roleId == null) {
            return;
        }
        QueryWrapper wrapper = Wrappers.query();
        wrapper.eq("role_id", roleId);
        sysRoleMenuDao.delete(wrapper);
    }

}
