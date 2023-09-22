package com.lhh.servermonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.servermonitor.dao.ScanSecurityHoleDao;
import com.lhh.servermonitor.service.ScanSecurityHoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("scanSecurityHoleService")
public class ScanSecurityHoleServiceImpl extends ServiceImpl<ScanSecurityHoleDao, ScanSecurityHoleEntity> implements ScanSecurityHoleService {

    @Autowired
    private ScanSecurityHoleDao scanSecurityHoleDao;

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public IPage<ScanSecurityHoleEntity> page(Map<String, Object> params) {
        IPage<ScanSecurityHoleEntity> page = this.page(
                new Query<ScanSecurityHoleEntity>().getPage(params),
                new QueryWrapper<ScanSecurityHoleEntity>()
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
    public List<ScanSecurityHoleEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<ScanSecurityHoleEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanSecurityHoleEntity> basicList(Map<String, Object> params) {
        return scanSecurityHoleDao.basicList(params);
    }

}
