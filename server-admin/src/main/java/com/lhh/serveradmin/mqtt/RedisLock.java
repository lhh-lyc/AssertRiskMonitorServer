package com.lhh.serveradmin.mqtt;

import com.alibaba.fastjson2.JSON;
import com.lhh.serveradmin.utils.JedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanProjectEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
public class RedisLock {

    @Autowired
    RedisTemplate redisTemplate;

    public void saveProjectRedis(ScanProjectEntity project) {
        // 每个人进来先要进行加锁，key值为"LOCK_PROJECT:id"
        String lockKey = String.format(CacheConst.REDIS_LOCK_PROJECT, project.getId());
        String value = UUID.randomUUID().toString().replace("-", "");
        try {
            // 为key加一个过期时间
            Boolean flag = redisTemplate.opsForValue().setIfAbsent(lockKey, value, 10L, TimeUnit.SECONDS);
            while (!flag) {
                log.info("admin加锁失败" + value);
                flag = redisTemplate.opsForValue().setIfAbsent(lockKey, value, 10L, TimeUnit.SECONDS);
            }
            log.info("admin加锁成功" + value);
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
            redisTemplate.delete(lockKey);
            log.info("monitor解锁成功" + value);
            redisTemplate.getConnectionFactory().getConnection().close();
        }
    }

}