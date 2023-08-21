package com.lhh.servermonitor.mqtt;

import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.service.ScanProjectService;
import com.lhh.servermonitor.utils.JedisUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(value = "projectData", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ProjectListener {

    @Autowired
    ScanProjectService scanProjectService;
    @Autowired
    RedisLock redisLock;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ScanProjectEntity project = (ScanProjectEntity) SerializationUtils.deserialize(bytes);
//        log.info("开始处理项目" + project.getQueueId());
        try {
            // mq分割project，合并缓存问题
            redisLock.saveProjectRedis(project);
            scanProjectService.saveProject(project);
//            JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()));

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
//            log.info("项目" + project.getQueueId() + "处理完毕");
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.error("项目" + project.getQueueId() + ":" + project.getHostList().get(0) + "处理失败", e);
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
