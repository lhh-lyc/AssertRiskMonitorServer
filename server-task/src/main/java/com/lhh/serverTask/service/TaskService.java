package com.lhh.serverTask.service;

import com.lhh.serverTask.mqtt.ParentDomainSender;
import com.lhh.serverTask.utils.RedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.jedis.JedisUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service("TaskService")
public class TaskService {

    @Value("${mqtt-setting.taskSubDomain-route-key}")
    private String taskSubDomainRouteKey;
    @Value("${mqtt-setting.taskSubDomain-pub-topic}")
    private String taskSubDomainPubTopic;

    @Autowired
    ScanHostService scanHostService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    ParentDomainSender parentDomainSender;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void weekTask(){
        // 如果缓存里没有正在处理的域名或ip，
        Boolean beginFlg = redisUtils.keyExist(String.format(CacheConst.REDIS_TASK_PARENT_DOMAIN, Const.STR_ASTERISK)) &&
                redisUtils.keyExist(String.format(CacheConst.REDIS_TASKING_IP, Const.STR_ASTERISK));
        if (beginFlg) {
            log.info("正在扫描中。。。");
            redisUtils.delKey(CacheConst.REDIS_NEXT_TASK);
            return;
        } else {
            if (!redisUtils.hasKey(CacheConst.REDIS_NEXT_TASK)) {
                LocalDate currentDate = LocalDate.now();
                // 计算两周后的日期
                LocalDate dateAfterTwoWeeks = currentDate.plus(Period.ofWeeks(2));
                String nextTime = dateAfterTwoWeeks.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                redisUtils.setString(CacheConst.REDIS_NEXT_TASK, nextTime);
                log.info("生成任务缓存，下次执行时间为" + nextTime);
                return;
            } else {
                String nextTime = redisUtils.getString(CacheConst.REDIS_NEXT_TASK);
                if (!LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).equals(nextTime)) {
                    return;
                }
            }
        }
        log.info("weekTask开始执行！");
        List<String> hostList = scanHostService.getParentList();
        parentDomainSender.sendParentToMqtt(hostList);
        redisUtils.delKey(CacheConst.REDIS_NEXT_TASK);
        log.info("weekTask投送域名完毕！");
    }

}
