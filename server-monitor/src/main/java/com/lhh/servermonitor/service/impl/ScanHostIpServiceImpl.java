package com.lhh.servermonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.ScanHostIpEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.servermonitor.dao.ScanHostIpDao;
import com.lhh.servermonitor.service.ScanHostIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("scanHostIpService")
public class ScanHostIpServiceImpl extends ServiceImpl<ScanHostIpDao, ScanHostIpEntity> implements ScanHostIpService {

    @Autowired
    private ScanHostIpDao scanHostIpDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanHostIpEntity> page(Map<String, Object> params) {
        IPage<ScanHostIpEntity> page = this.page(
                new Query<ScanHostIpEntity>().getPage(params),
                new QueryWrapper<ScanHostIpEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanHostIpEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<ScanHostIpEntity> list = list(wrapper);
        return list;
    }

}
