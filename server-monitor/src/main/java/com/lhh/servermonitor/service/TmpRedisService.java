package com.lhh.servermonitor.service;

import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.HostCompanyEntity;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class TmpRedisService {

    @Autowired
    HostCompanyService hostCompanyService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redisson;

    public String getDomainScanPorts(String domain) {
        String scanPorts = hostCompanyService.getScanPorts(domain);
        if (StringUtils.isEmpty(scanPorts)) {
            String lockKey = String.format(CacheConst.REDIS_LOCK_HOST_INFO, domain);
            RLock lock = redisson.getLock(lockKey);
            try {
                lock.lock();
                scanPorts = hostCompanyService.getScanPorts(domain);
                if (StringUtils.isEmpty(scanPorts)) {
                    HostCompanyEntity hostInfo = hostCompanyService.setHostInfo(domain);
                    scanPorts = StringUtils.isEmpty(hostInfo.getScanPorts()) ? Const.STR_EMPTY : hostInfo.getScanPorts();
                }
            } catch (Exception e) {
                log.error(domain + "主域名信息更新报错", e);
            } finally {
                // 判断当前线程是否持有锁
                if (lock.isHeldByCurrentThread()) {
                    //释放当前锁
                    lock.unlock();
                }
            }
        }
        return scanPorts;
    }

}
