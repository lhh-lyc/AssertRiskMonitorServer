package com.lhh.serverinfocommon.service.imsp.sys;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.SysRoleEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.sys.SysRoleDao;
import com.lhh.serverinfocommon.service.sys.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("sysRoleService")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleDao, SysRoleEntity> implements SysRoleService {

    @Autowired
    private SysRoleDao sysRoleDao;

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public IPage<SysRoleEntity> page(Map<String, Object> params) {
        IPage<SysRoleEntity> page = this.page(
                new Query<SysRoleEntity>().getPage(params),
                new QueryWrapper<SysRoleEntity>()
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
    public List<SysRoleEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query()
                .eq("del_flg", Const.INTEGER_0);
        List<SysRoleEntity> list = list(wrapper);
        return list;
    }

}
