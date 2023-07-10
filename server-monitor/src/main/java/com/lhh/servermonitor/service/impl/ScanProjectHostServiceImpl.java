package com.lhh.servermonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.NetErrorDataEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.servermonitor.dao.ScanProjectHostDao;
import com.lhh.servermonitor.service.NetErrorDataService;
import com.lhh.servermonitor.service.ScanProjectHostService;
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
    private NetErrorDataService netErrorDataService;
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
        log.info("开始更新project_host=" + domain + "数据状态");
        // 域名下所有ip全部扫描完成，修改对应域名的数据状态 is_scanning=0
        String lockKey = String.format(CacheConst.REDIS_LOCK_PROJECT_DOMAIN_SCAN_CHANGE, domain);
        RLock lock = redisson.getLock(lockKey);
        boolean success = true;
        try {
            success = lock.tryLock(5, TimeUnit.SECONDS);
            if (success) {
                scanProjectHostDao.updateEndScanDomain(domain);
            }
        } catch (Exception e) {
            NetErrorDataEntity err = NetErrorDataEntity.builder()
                    .obj(domain).type(Const.INTEGER_1)
                    .build();
            netErrorDataService.save(err);
            log.error("更新project_host=" + domain + "数据状态出现问题,异常详情：", e);
        } finally {
            // 判断当前线程是否持有锁
            if (success && lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
        log.info("更新结束project_host=" + domain + "数据状态");
    }

    @Override
    public void endScanDomain(String domain) {
        log.info("开始补充更新project_host=" + domain + "数据状态");
        // 域名下所有ip全部扫描完成，修改对应域名的数据状态 is_scanning=0
        String lockKey = String.format(CacheConst.REDIS_LOCK_PROJECT_DOMAIN_SCAN_CHANGE, domain);
        RLock lock = redisson.getLock(lockKey);
        boolean success = true;
        try {
            success = lock.tryLock(5, TimeUnit.SECONDS);
            if (success) {
                scanProjectHostDao.updateEndScanDomain(domain);
            }
        } catch (Exception e) {
            log.error("补充更新project_host=" + domain + "数据状态出现问题,异常详情：", e);
        } finally {
            // 判断当前线程是否持有锁
            if (success && lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
        log.info("补充更新结束project_host=" + domain + "数据状态");
    }

}
