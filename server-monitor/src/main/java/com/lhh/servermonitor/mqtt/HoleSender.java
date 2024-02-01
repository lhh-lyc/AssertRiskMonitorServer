package com.lhh.servermonitor.mqtt;

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

@Slf4j
@Component
public class HoleSender {

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

    public void sendHoleToMqtt(ScanParamDto dto) {
        try {
            log.info(dto.getSubDomain() + Const.STR_COLON + dto.getPort() + "漏洞子域名端口开始投递");
            CorrelationData correlationId = new CorrelationData(dto.getProjectId() + Const.STR_COLON + dto.getSubDomain());
            rabbitTemplate.convertAndSend(exchange, holeRouteKey, SerializationUtils.serialize(dto), correlationId);
        } catch (Exception e) {
            log.error(String.format("推送漏洞子域名失败：项目%s_子域名%s", dto.getProjectId(), dto.getSubDomain()) + e);
        }
    }

}
