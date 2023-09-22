package com.lhh.servermonitor.mqtt;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.service.*;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(value = "scanningIpData", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ScanningIpListener {

    @Autowired
    SysDictService sysDictService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    ScanHostPortService scanHostPortService;
    @Autowired
    ScanHoleService scanHoleService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    HostCompanyService hostCompanyService;
    @Autowired
    RedisLock redisLock;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ScanParamDto dto = (ScanParamDto) SerializationUtils.deserialize(bytes);
        try {
            log.info("扫描ip端口：" + JSON.toJSONString(dto));
            String company = hostCompanyService.getCompany(dto.getSubIp());
            List<ScanHostEntity> exitIpList = scanHostService.getIpByIpList(Arrays.asList(IpLongUtils.ipToLong(dto.getSubIp())));
            if (CollectionUtils.isEmpty(exitIpList)) {
                ScanHostEntity scanIp = ScanHostEntity.builder()
                        .domain(dto.getSubIp()).parentDomain(dto.getSubIp())
                        .ip(dto.getSubIp()).ipLong(IpLongUtils.ipToLong(dto.getSubIp()))
                        .company(company)
                        .type(Const.INTEGER_2)
                        .isMajor(Const.INTEGER_0)
                        .isDomain(Const.INTEGER_0)
                        .isScanning(Const.INTEGER_0)
                        .build();
                scanHostService.save(scanIp);
            }
            scanPortInfoService.scanIpsPortList(dto);
            scanHostPortService.scanSingleHostPortList(dto.getSubIp());
            scanHoleService.scanHoleList(dto.getProjectId(), dto.getSubIp());
            redisLock.delDomainRedis(dto.getProjectId(), dto.getSubIp(), dto.getSubIp(), dto.getScanPorts());
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
