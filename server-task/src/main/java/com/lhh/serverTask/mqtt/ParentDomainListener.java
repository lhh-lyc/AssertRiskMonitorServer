package com.lhh.serverTask.mqtt;

import com.lhh.serverTask.service.ParentDomainService;
import com.lhh.serverTask.utils.RedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.dto.ReScanDto;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(value = "taskParentDomain", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ParentDomainListener {

    @Autowired
    ParentDomainService parentDomainService;
    @Autowired
    RedisUtils redisUtils;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ReScanDto dto = (ReScanDto) SerializationUtils.deserialize(bytes);
        log.info("开始处理重扫队列" + dto.getQueueId());
        try {
            parentDomainService.scanDomain(dto);
            //这里停留一秒，是因为如果一组队列全是ip，直接就发送到mq然后进行下面的缓存删除步骤，
            // 此时TASK_PARENT_DOMAIN缓存还没有存进去就出现了先删除后保存的操作，使缓存出现脏数据
            Thread.sleep(1000);
            if (!CollectionUtils.isEmpty(dto.getHostList())) {
                List<String> keyList = new ArrayList<>();
                for (String h : dto.getHostList()) {
                    keyList.add(String.format(CacheConst.REDIS_TASK_PARENT_DOMAIN, h));
                }
                redisUtils.delStringBatch(keyList);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            log.info("队列" + dto.getQueueId() + "处理完毕");
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.error("队列" + dto.getQueueId() + "处理失败", e);
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
