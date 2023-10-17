package com.lhh.servermonitor.service;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.HostCompanyEntity;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TmpRedisService {

    @Autowired
    HostCompanyService hostCompanyService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redisson;

    public HostCompanyEntity getHostInfo(String domain) {
        HostCompanyEntity hostInfo = hostCompanyService.getHostInfo(domain);
        if (hostInfo != null) {
            String lockKey = String.format(CacheConst.REDIS_LOCK_HOST_INFO, domain);
            RLock lock = redisson.getLock(lockKey);
            try {
                lock.lock();
                hostInfo = hostCompanyService.getHostInfo(domain);
                if (hostInfo == null) {
                    hostInfo = hostCompanyService.setHostInfo(domain);
                }
            } catch (Exception e) {
                log.error(domain + "主域名信息查询报错", e);
            } finally {
                // 判断当前线程是否持有锁
                if (lock.isHeldByCurrentThread()) {
                    //释放当前锁
                    lock.unlock();
                }
            }
        }
        return hostInfo;
    }

    public List<HostCompanyEntity> getHostInfoList(List<String> domainList) {
        List<HostCompanyEntity> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(domainList)) {
            return result;
        }
        List<String> newList = new ArrayList<>();
        for (String domain : domainList) {
            String value = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_HOST_INFO, domain));
            if (!StringUtils.isEmpty(value)) {
                HostCompanyEntity hostInfo = JSON.parseObject(value, HostCompanyEntity.class);
                result.add(hostInfo);
                newList.remove(domain);
            }
        }
        List<HostCompanyEntity> hostInfoList = hostCompanyService.setHostInfoList(newList);
        result.addAll(hostInfoList);
        return result;
    }

}
