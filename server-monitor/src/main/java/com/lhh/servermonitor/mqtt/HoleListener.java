package com.lhh.servermonitor.mqtt;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.dao.ScanProjectDao;
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
public class HoleListener {

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
            log.info("扫描漏洞入参：" + JSON.toJSONString(dto));
            ScanProjectEntity oldProject = scanProjectDao.selectById(dto.getProjectId());
            if (oldProject == null) {
                redisLock.delDomainPortRedis(dto.getProjectId(), dto.getDomain(), dto.getSubDomain(), dto.getScanPorts(), dto.getPort());
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                log.info("项目id=" + dto.getProjectId() + "已被删除,不处理漏洞" + dto.getSubDomain());
                return;
            }
//            scanHoleService.scanHoleList(dto.getProjectId(), dto.getSubDomain());
            scanHoleService.scanHoleSingle(dto.getProjectId(), dto.getSubDomain(), dto.getPort());
            log.info("漏洞扫描完成，删除key:" + dto.getProjectId() + "_" + dto.getSubDomain() + "_" + dto.getPort());
            redisLock.delDomainPortRedis(dto.getProjectId(), dto.getDomain(), dto.getSubDomain(), dto.getScanPorts(), dto.getPort());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            log.info("扫描漏洞完成：项目id：" + dto.getProjectId() + ":" + dto.getSubDomain() + "_" + dto.getPort());
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.error("扫描漏洞失败：项目id：" + dto.getProjectId() + ":" + dto.getSubDomain() + e);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
