package com.lhh.serverTask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverTask.dao.ScanPortDao;
import com.lhh.serverTask.service.ScanPortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service("scanPortService")
public class ScanPortServiceImpl extends ServiceImpl<ScanPortDao, ScanPortEntity> implements ScanPortService {

    @Autowired
    private ScanPortDao scanPortDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanPortEntity> page(Map<String, Object> params) {
        IPage<ScanPortEntity> page = this.page(
                new Query<ScanPortEntity>().getPage(params),
                new QueryWrapper<ScanPortEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanPortEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<ScanPortEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanPortEntity> getByIpList(List<String> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return new ArrayList<>();
        }
        QueryWrapper wrapper = Wrappers.query()
                .in("ip", hostList);
        List<ScanPortEntity> list = list(wrapper);
        return list;
    }

}