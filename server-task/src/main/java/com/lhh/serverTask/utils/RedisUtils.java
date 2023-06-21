package com.lhh.serverTask.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RedisUtils {

    @Autowired
    RedissonClient redisson;
    @Autowired
    RedisTemplate redisTemplate;

    public void addSet(String lockKey, String key, List list) {
        RLock lock = redisson.getLock(lockKey);
        try {
            lock.lock();
            redisTemplate.opsForSet().add(key, list);
        } finally {
            lock.unlock();
        }
    }

    public void delSetMembers(String lockKey, String key, List list) {
        RLock lock = redisson.getLock(lockKey);
        try {
            lock.lock();
            redisTemplate.opsForSet().remove(key, list);
        } finally {
            lock.unlock();
        }
    }

}
