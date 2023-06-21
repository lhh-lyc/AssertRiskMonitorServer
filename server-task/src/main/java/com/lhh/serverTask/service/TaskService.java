package com.lhh.serverTask.service;

import com.lhh.serverTask.mqtt.ParentDomainSender;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.entity.ScanHostEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service("TaskService")
public class TaskService {

    @Autowired
    ScanHostService scanHostService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    ParentDomainSender parentDomainSender;

    public void weekTask(){
        log.info("weekTask开始执行！");
        List<String> hostList = scanHostService.getParentList();
        parentDomainSender.sendParentToMqtt(hostList);
        log.info("weekTask投送域名完毕！");
    }

}
