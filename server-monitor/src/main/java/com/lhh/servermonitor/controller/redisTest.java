package com.lhh.servermonitor.controller;

import com.lhh.servermonitor.utils.JedisUtils;
import org.apache.shiro.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
public class redisTest {

    @Autowired
    RedisTemplate redisTemplate;

    @GetMapping("test2")
    public void test2(String key) {
        // 加锁
    }

    @GetMapping("test")
    public void test(Integer num) {
        Lock lock = new ReentrantLock();
        for (int i = 0; i < num; i++) {
            int finalI = i;
            Executors.newFixedThreadPool(10).execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        doing();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void doing() {
        // 每个人进来先要进行加锁，key值为"lock"
        String value = UUID.randomUUID().toString().replace("-", "");
        try {
            // 为key加一个过期时间
            Boolean flag = redisTemplate.opsForValue().setIfAbsent("LOCK", value, 10L, TimeUnit.SECONDS);

            while (!flag) {
                flag = redisTemplate.opsForValue().setIfAbsent("LOCK", value, 10L, TimeUnit.SECONDS);
            }
            Object result = redisTemplate.opsForValue().get("IS_TODAY");
            int total = result == null ? 0 : Integer.parseInt(String.valueOf(result));
            redisTemplate.opsForValue().set("IS_TODAY", total + 1);
        } finally {
//            if(redisTemplate.opsForValue().get("LOCK").equals(value)){
            redisTemplate.delete("LOCK");
//            }
        }
    }

}
