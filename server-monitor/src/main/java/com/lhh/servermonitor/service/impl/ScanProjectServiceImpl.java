package com.lhh.servermonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.servermonitor.dao.ScanProjectDao;
import com.lhh.servermonitor.service.ScanProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("scanProjectService")
public class ScanProjectServiceImpl extends ServiceImpl<ScanProjectDao, ScanProjectEntity> implements ScanProjectService {

    @Autowired
    private ScanProjectDao scanProjectDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanProjectEntity> page(Map<String, Object> params) {
        IPage<ScanProjectEntity> page = this.page(
                new Query<ScanProjectEntity>().getPage(params),
                new QueryWrapper<ScanProjectEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanProjectEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<ScanProjectEntity> list = list(wrapper);
        return list;
    }

}
