package com.lhh.servermonitor.mqtt;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.service.ScanProjectService;
import com.lhh.servermonitor.sync.SyncService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    @Autowired
    SyncService syncService;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ScanProjectEntity project = (ScanProjectEntity) SerializationUtils.deserialize(bytes);
        try {
            log.info(JSON.toJSONString(project));
//            redisLock.saveProjectRedis(project);
            scanProjectService.saveProject(project);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            log.info("项目" + project.getQueueId() + "处理完毕");
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.info("项目" + project.getQueueId() + "处理失败");
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
