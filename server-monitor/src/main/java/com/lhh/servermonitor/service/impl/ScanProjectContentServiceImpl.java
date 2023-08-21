package com.lhh.servermonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.servermonitor.dao.ScanProjectContentDao;
import com.lhh.servermonitor.service.ScanProjectContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service("scanProjectContentService")
public class ScanProjectContentServiceImpl extends ServiceImpl<ScanProjectContentDao, ScanProjectContentEntity> implements ScanProjectContentService {

    @Autowired
    private ScanProjectContentDao scanProjectContentDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanProjectContentEntity> page(Map<String, Object> params) {
        IPage<ScanProjectContentEntity> page = this.page(
                new Query<ScanProjectContentEntity>().getPage(params),
                new QueryWrapper<ScanProjectContentEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanProjectContentEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query()
                .eq("del_flg", Const.INTEGER_0);
        wrapper.eq(params.get("projectId") != null, "project_id", params.get("projectId"));
        wrapper.eq(params.get("inputHost") != null, "input_host", params.get("inputHost"));
        List<ScanProjectContentEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanProjectContentEntity> getExitHostList(Long projectId, List<String> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return new ArrayList<>();
        }
        QueryWrapper wrapper = Wrappers.query()
                .in("project_id", projectId)
                .in("input_host", hostList)
                .eq("del_flg", Const.INTEGER_0);
        List<ScanProjectContentEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<Long> getProjectIdList(String domain) {
        return scanProjectContentDao.getProjectIdList(domain);
    }

    @Override
    public void updateStatus(List<ScanProjectContentEntity> list) {
        scanProjectContentDao.updateStatus(list);
    }

}
