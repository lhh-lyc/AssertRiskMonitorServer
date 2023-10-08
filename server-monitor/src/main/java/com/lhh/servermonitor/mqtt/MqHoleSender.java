package com.lhh.servermonitor.mqtt;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.servermonitor.controller.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MqHoleSender {

    @Value("${mqtt-setting.exchange}")
    private String exchange;
    @Value("${mqtt-setting.hole-route-key}")
    private String holeRouteKey;

    @Autowired
    AmqpTemplate amqpTemplate;
    @Autowired
    RedisLock redisLock;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendScanningHoleToMqtt(ScanParamDto dto) {
        try {
            log.info(dto.getSubDomain() + "已扫描漏洞子域名开始投递");
            CorrelationData correlationId = new CorrelationData(dto.getProjectId() + Const.STR_COLON + dto.getSubDomain());
            rabbitTemplate.convertAndSend(exchange, holeRouteKey, SerializationUtils.serialize(dto), correlationId);
            redisLock.addDomainRedis(dto.getProjectId(), dto.getDomain(), dto.getSubDomain());
        } catch (Exception e) {
            log.error(String.format("推送已扫描端口域名失败：项目%s_子域名%s", dto.getProjectId(), dto.getSubDomain()) + e);
        }
    }

}
