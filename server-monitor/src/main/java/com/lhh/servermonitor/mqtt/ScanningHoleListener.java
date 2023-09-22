package com.lhh.servermonitor.mqtt;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.service.ScanHoleService;
import com.lhh.servermonitor.service.ScanHostPortService;
import com.lhh.servermonitor.service.ScanPortInfoService;
import com.lhh.servermonitor.service.SysDictService;
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
        value = @Queue(value = "scanningHoleData", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ScanningHoleListener {

    @Autowired
    SysDictService sysDictService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    ScanHostPortService scanHostPortService;
    @Autowired
    ScanHoleService scanHoleService;
    @Autowired
    RedisLock redisLock;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ScanParamDto dto = (ScanParamDto) SerializationUtils.deserialize(bytes);
        try {
            log.info("扫描ip端口：" + JSON.toJSONString(dto));
            scanHoleService.scanHoleList(dto.getProjectId(), dto.getSubDomain());
            redisLock.delDomainRedis(dto.getProjectId(), dto.getSubDomain(), dto.getSubDomain(), dto.getScanPorts());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.error("扫描ip端口失败：" + e);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
