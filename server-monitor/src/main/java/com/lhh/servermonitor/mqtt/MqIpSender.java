package com.lhh.servermonitor.mqtt;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.utils.CopyUtils;
import com.lhh.servermonitor.config.RabbitMqConfig;
import com.lhh.servermonitor.controller.RedisLock;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class MqIpSender {

    @Value("${mqtt-setting.exchange}")
    private String exchange;
    @Value("${mqtt-setting.ip-route-key}")
    private String ipRouteKey;

    @Autowired
    AmqpTemplate amqpTemplate;
    @Autowired
    RedisLock redisLock;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendScanningIpToMqtt(List<ScanParamDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return;
        }
        Boolean reset = false;
        List<ScanParamDto> resetList = new ArrayList<>();
        Integer num = Const.INTEGER_0;
        try {
            //6. 将消息发送到队列
            for (ScanParamDto dto : dtoList) {
                num++;
                log.info(dto.getSubIp() + "ip开始投递");
                CorrelationData correlationId = new CorrelationData(dto.toString());
                //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
                rabbitTemplate.convertAndSend(exchange, ipRouteKey, SerializationUtils.serialize(dto), correlationId);
                redisLock.addDomainRedis(dto.getProjectId(), dto.getSubIp(), dto.getSubIp());
            }
        } catch (Exception e) {
            reset = true;
            resetList.add(dtoList.get(num));
            log.error("项目id=" + dtoList.get(0).getProjectId()+ "推送ip-mq产生异常",e);
        } finally {
            if (reset) {
                sendScanningIpToMqtt(resetList);
            }
        }
    }

}
