package com.lhh.serverinfocommon.service.imsp.scan;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.scan.ScanProjectHostDao;
import com.lhh.serverinfocommon.service.scan.ScanProjectHostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("scanProjectHostService")
public class ScanProjectHostServiceImpl extends ServiceImpl<ScanProjectHostDao, ScanProjectHostEntity> implements ScanProjectHostService {

    @Autowired
    private ScanProjectHostDao scanProjectHostDao;
    @Autowired
    RedissonClient redisson;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanProjectHostEntity> page(Map<String, Object> params) {
        IPage<ScanProjectHostEntity> page = this.page(
                new Query<ScanProjectHostEntity>().getPage(params),
                new QueryWrapper<ScanProjectHostEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanProjectHostEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        wrapper.eq(params.get("projectId") != null, "project_id", params.get("projectId"));
        wrapper.eq(params.get("host") != null, "host", params.get("host"));
        List<ScanProjectHostEntity> list = list(wrapper);
        return list;
    }

    /**
     * 查询列表数据
     * @param projectId, host
     * @return
     */
    @Override
    public List<ScanProjectHostEntity> selByProIdAndHost(Long projectId, String host) {
        QueryWrapper wrapper = Wrappers.query();
        wrapper.eq(projectId != null, "project_id", projectId);
        wrapper.eq(!StringUtils.isEmpty(host), "host", host);
        List<ScanProjectHostEntity> list = list(wrapper);
        return list;
    }

}
