package com.lhh.serverscanhole.mqtt;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverscanhole.controller.RedisLock;
import com.lhh.serverscanhole.dao.ScanProjectDao;
import com.lhh.serverscanhole.service.ScanHoleService;
import com.lhh.serverscanhole.service.ScanHostPortService;
import com.lhh.serverscanhole.service.ScanPortInfoService;
import com.lhh.serverscanhole.service.SysDictService;
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
        value = @Queue(value = "exitHoleData", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ExitHoleListener {

    @Autowired
    SysDictService sysDictService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    ScanHostPortService scanHostPortService;
    @Autowired
    ScanHoleService scanHoleService;
    @Autowired
    private ScanProjectDao scanProjectDao;
    @Autowired
    RedisLock redisLock;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ScanParamDto dto = (ScanParamDto) SerializationUtils.deserialize(bytes);
        try {
            log.info("已存扫描漏洞入参：" + JSON.toJSONString(dto));
            ScanProjectEntity oldProject = scanProjectDao.selectById(dto.getProjectId());
            if (oldProject == null) {
                redisLock.delDomainRedis(dto.getProjectId(), dto.getDomain(), dto.getSubDomain(), dto.getScanPorts());
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                log.info("项目id=" + dto.getProjectId() + "已被删除,不处理漏洞" + dto.getSubDomain());
                return;
            }
            scanHoleService.scanHoleList(dto.getProjectId(), dto.getSubDomain());
            redisLock.delDomainRedis(dto.getProjectId(), dto.getDomain(), dto.getSubDomain(), dto.getScanPorts());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.error("已存扫描漏洞失败：" + e);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
