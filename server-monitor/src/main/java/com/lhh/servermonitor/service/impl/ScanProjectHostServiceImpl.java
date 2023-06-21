package com.lhh.servermonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.servermonitor.dao.ScanProjectHostDao;
import com.lhh.servermonitor.service.ScanProjectHostService;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


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
        QueryWrapper wrapper = Wrappers.query()
                .eq("del_flg", Const.INTEGER_0);
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
        QueryWrapper wrapper = Wrappers.query()
                .eq("del_flg", Const.INTEGER_0);
        wrapper.eq(projectId != null, "project_id", projectId);
        wrapper.eq(!StringUtils.isEmpty(host), "host", host);
        List<ScanProjectHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public void updateEndScanDomain(String domain) {
        // 域名下所有ip全部扫描完成，修改对应域名的数据状态 is_scanning=0
        String lockKey = String.format(CacheConst.REDIS_LOCK_PROJECT_DOMAIN_SCAN_CHANGE, domain);
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {
            scanProjectHostDao.updateEndScanDomain(domain);
        } catch (Exception e) {
            log.error("更新project_host表domain扫描状态出错", e);
        } finally {
            lock.unlock();
        }
    }

}
