package com.lhh.servermonitor.controller;

import com.alibaba.fastjson2.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.servermonitor.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class RedisLock {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedissonClient redisson;

    public void saveProjectRedis(ScanProjectEntity project) {
        // 每个人进来先要进行加锁，key值为"LOCK_PROJECT:id"
        String lockKey = String.format(CacheConst.REDIS_LOCK_PROJECT, project.getId());
        RLock lock = redisson.getLock(lockKey);
        try {
            lock.lock();
            String projectRedisValue = JedisUtils.getStr(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getUserId() + Const.STR_TITLE + project.getName()));
            log.info("redis前数据：" + projectRedisValue);
            if (!StringUtils.isEmpty(projectRedisValue)) {
                ScanProjectEntity redisProject = JSON.parseObject(projectRedisValue, ScanProjectEntity.class);
                redisProject.getHostList().addAll(project.getHostList());
                JedisUtils.setJson(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getUserId() + Const.STR_TITLE + project.getName()), JSON.toJSONString(redisProject));
            } else {
                JedisUtils.setJson(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getUserId() + Const.STR_TITLE + project.getName()), JSON.toJSONString(project));
            }
            log.info("redis后数据：" + JedisUtils.getStr(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getUserId() + Const.STR_TITLE + project.getName())));
        } finally {
            lock.unlock();
            redisTemplate.getConnectionFactory().getConnection().close();
        }
    }

}