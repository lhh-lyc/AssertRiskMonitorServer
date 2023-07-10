package com.lhh.servermonitor.controller;

import com.alibaba.fastjson2.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.entity.ScanProjectEntity;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RedisLock {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redisson;

    public void saveProjectRedis(ScanProjectEntity project) {
        // 每个人进来先要进行加锁，key值为"LOCK_PROJECT:id"
        String lockKey = String.format(CacheConst.REDIS_LOCK_PROJECT, project.getId());
        RLock lock = redisson.getLock(lockKey);
        boolean success = true;
        try {
            success = lock.tryLock(5, TimeUnit.SECONDS);
            if (success) {
                String projectStr = redisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()));
                if (!StringUtils.isEmpty(projectStr)) {
                    log.info("项目redis前数据：" + projectStr);
                    ScanProjectEntity redisProject = JSON.parseObject(projectStr, ScanProjectEntity.class);
                    redisProject.getHostList().addAll(project.getHostList());
                    redisProject.setHostList(redisProject.getHostList().stream().distinct().collect(Collectors.toList()));
                    redisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()), JSON.toJSONString(redisProject));
                    log.info("项目redis后数据：" + JSON.toJSONString(redisProject));
                } else {
                    redisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()), JSON.toJSONString(project));
                    log.info("项目redis后数据：" + JSON.toJSONString(project));
                }
            }
        } catch (Exception e) {
            log.error("项目增加域名报错", e);
        } finally {
            // 判断当前线程是否持有锁
            if (success && lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
    }

}