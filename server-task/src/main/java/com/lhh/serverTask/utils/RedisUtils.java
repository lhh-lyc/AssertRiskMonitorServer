package com.lhh.serverTask.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisUtils {

    @Autowired
    RedissonClient redisson;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

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

    public void setString(String key, String value){
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void setString(String key, String value, Long time){
        stringRedisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    public String getString(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

    public Boolean keyExist(String key){
        Set<String> set = stringRedisTemplate.keys(key);
        if (CollectionUtils.isEmpty(set)) {
            return false;
        }
        return true;
    }

    public Boolean hasKey(String key){
        return stringRedisTemplate.hasKey(key);
    }

    public void addStringBatch(Map<String, String> map){
        stringRedisTemplate.executePipelined(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    stringRedisTemplate.opsForValue().set(entry.getKey(), entry.getValue());
                }
                return null;
            }
        });
    }

    public void delKey(String key){
        stringRedisTemplate.delete(key);
    }

    public void delStringBatch(List<String> list){
        stringRedisTemplate.executePipelined(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                for (String key : list) {
                    stringRedisTemplate.delete(key);
                }
                return null;
            }
        });
    }

}
