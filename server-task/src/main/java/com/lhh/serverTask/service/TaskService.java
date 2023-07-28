package com.lhh.serverTask.service;

import com.alibaba.fastjson2.JSON;
import com.lhh.serverTask.dao.HostCompanyDao;
import com.lhh.serverTask.mqtt.MqCompanySender;
import com.lhh.serverTask.mqtt.ParentDomainSender;
import com.lhh.serverTask.utils.RedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.utils.HttpUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.jedis.JedisUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Service("TaskService")
public class TaskService {

    @Value("${mqtt-setting.taskSubDomain-route-key}")
    private String taskSubDomainRouteKey;
    @Value("${mqtt-setting.taskSubDomain-pub-topic}")
    private String taskSubDomainPubTopic;

    @Autowired
    RedisUtils redisUtils;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redisson;
    @Autowired
    ParentDomainSender parentDomainSender;
    @Autowired
    MqCompanySender mqCompanySender;
    @Autowired
    ScanHostService scanHostService;

    public void weekTask() {
        RLock lock = redisson.getLock(CacheConst.REDIS_BEGIN_TASK);
        boolean success = true;
        try {
            success = lock.tryLock();
            if (success) {
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
        } catch (Exception e) {
            log.error("weekTask投送域名报错", e);
        } finally {
            if (success && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
