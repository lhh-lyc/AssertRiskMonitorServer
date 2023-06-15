package com.lhh.servermonitor.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private Integer port;
    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public RedissonClient redisson(){
        Config config = new Config();
        config.useSingleServer().setAddress(("redis://"+host+":"+port)).setPassword(password).setDatabase(0);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
