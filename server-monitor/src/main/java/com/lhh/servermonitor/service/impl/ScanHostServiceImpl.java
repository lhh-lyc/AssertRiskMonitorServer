package com.lhh.servermonitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.NetErrorDataEntity;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.Query;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.dao.ScanHostDao;
import com.lhh.servermonitor.dao.ScanProjectContentDao;
import com.lhh.servermonitor.dao.ScanProjectHostDao;
import com.lhh.servermonitor.service.NetErrorDataService;
import com.lhh.servermonitor.service.ScanHostService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("scanHostService")
public class ScanHostServiceImpl extends ServiceImpl<ScanHostDao, ScanHostEntity> implements ScanHostService {

    @Autowired
    private ScanHostDao scanHostDao;
    @Autowired
    private NetErrorDataService netErrorDataService;
    @Autowired
    private ScanProjectContentDao scanProjectContentDao;
    @Autowired
    RedissonClient redisson;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanHostEntity> page(Map<String, Object> params) {
        IPage<ScanHostEntity> page = this.page(
                new Query<ScanHostEntity>().getPage(params),
                new QueryWrapper<ScanHostEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanHostEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query()
                .eq(params.get("parentDomain") != null, "parent_domain", params.get("parentDomain"))
                .eq(params.get("ipLong") != null, "ip_long", params.get("ipLong"));
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanHostEntity> getByParentDomainList(List<String> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return new ArrayList<>();
        }
        List<ScanHostEntity> list = scanHostDao.getByParentDomainList(hostList);
        return list;
    }

    @Override
    public List<ScanHostEntity> getByDomainList(List<String> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return new ArrayList<>();
        }
        QueryWrapper wrapper = Wrappers.query()
                .in("domain", hostList)
                .eq("del_flg", Const.INTEGER_0);
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanHostEntity> getByIpList(List<Long> ipList, String domain) {
        if (CollectionUtils.isEmpty(ipList)) {
            return new ArrayList<>();
        }
        List<ScanHostEntity> list = scanHostDao.getByIpList(ipList, domain);
        return list;
    }

    @Override
    public List<ScanHostEntity> getIpByIpList(List<Long> ipList) {
        return scanHostDao.getIpByIpList(ipList);
    }

    @Override
    public void updateEndScanDomain(String domain) {
        // 修改所有域名解析为当前ip的数据状态 is_scanning=0
        String lockKey = String.format(CacheConst.REDIS_LOCK_DOMAIN_SCAN_CHANGE, domain);
        RLock lock = redisson.getLock(lockKey);
        boolean success = true;
        try {
            success = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (success) {
                scanHostDao.updateEndScanDomain(domain);
            }
        } catch (Exception e) {
            log.error("批量更新host表domain状态扫描状态出错", e);
        } finally {
            // 判断当前线程是否持有锁
            if (success && lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
    }

    @Override
    public void updateEndScanIp(String domain, String ip, Long ipLong, String scanPorts){
        log.info("开始更新host表" + domain + ":" + ip + "(" + ipLong + ")数据状态");
        // 修改所有域名解析为当前ip的数据状态 is_scanning=0
        String lockKey = String.format(CacheConst.REDIS_LOCK_IP_SCAN_CHANGE, ipLong);
        RLock lock = redisson.getLock(lockKey);
        boolean success = true;
        try {
            success = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (success) {
                scanHostDao.updateEndScanIp(ipLong, scanPorts);
            }
        } catch (Exception e) {
            NetErrorDataEntity err = NetErrorDataEntity.builder()
                    .obj(ipLong.toString()).scanPorts(scanPorts).type(Const.INTEGER_2)
                    .build();
            netErrorDataService.save(err);
            log.error("更新host表" + domain + ":" + ip + "(" + ipLong + ")扫描状态出错", e);
        } finally {
            // 判断当前线程是否持有锁
            if (success && lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
        log.info("更新结束host表" + domain + ":" + ip + "(" + ipLong + ")数据状态");
    }

    @Override
    public void returnScanStatus(Long ipLong) {
        String lockKey = String.format(CacheConst.REDIS_LOCK_IP_SCAN_RETURN, ipLong);
        RLock lock = redisson.getLock(lockKey);
        boolean success = true;
        try {
            success = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (success) {
                scanHostDao.returnScanStatus(ipLong);
            }
        } catch (Exception e) {
//            NetErrorDataEntity err = NetErrorDataEntity.builder()
//                    .obj(ipLong.toString()).type(Const.INTEGER_3)
//                    .build();
//            netErrorDataService.save(err);
            log.error("更新host表ip重扫状态出错", e);
        } finally {
            // 判断当前线程是否持有锁
            if (success && lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
    }

    @Override
    public void saveBatch(List<ScanHostEntity> list) {
        scanHostDao.saveBatch(list);
    }

    @Override
    public List<ScanHostEntity> basicList(Map<String, Object> params) {
        return scanHostDao.basicList(params);
    }

    @Override
    public void endScanIp(Long ipLong, String scanPorts) {
        log.info("开始补充更新host表" + ipLong + "数据状态");
        String lockKey = String.format(CacheConst.REDIS_LOCK_IP_SCAN_CHANGE, ipLong);
        RLock lock = redisson.getLock(lockKey);
        boolean success = true;
        try {
            success = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (success) {
                scanHostDao.updateEndScanIp(ipLong, scanPorts);
            }
        } catch (Exception e) {
            log.error("补充更新host表" + ipLong + "扫描状态出错", e);
        } finally {
            // 判断当前线程是否持有锁
            if (success && lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
        log.info("补充更新结束host表" + ipLong + "数据状态");
    }

}
