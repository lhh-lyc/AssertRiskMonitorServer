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
public class HostSender {

    @Value("${mqtt-setting.exchange}")
    private String exchange;
    @Value("${mqtt-setting.host-route-key}")
    private String hostRouteKey;

    @Autowired
    AmqpTemplate amqpTemplate;
    @Autowired
    RedisLock redisLock;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendScanningHostToMqtt(List<ScanParamDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return;
        }
        Boolean reset = false;
        List<ScanParamDto> resetList = new ArrayList<>();
        Integer num = Const.INTEGER_0;
        try {
            String domain = dtoList.get(0).getHost();
            Long projectId = dtoList.get(0).getProjectId();
            // 先加入缓存，消息消费完会删除缓存
            // 避免先删完缓存，再加入缓存导致子域名残留的情况
            for (ScanParamDto dto : dtoList) {
                redisLock.addDomainRedis(projectId, domain, dto.getSubDomain());
            }
            for (ScanParamDto dto : dtoList) {
                num++;
                log.info(dto.getSubDomain() + "域名开始投递");
                CorrelationData correlationId = new CorrelationData(dto.toString());
                //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
                rabbitTemplate.convertAndSend(exchange, hostRouteKey, SerializationUtils.serialize(dto), correlationId);
            }
        } catch (Exception e) {
            reset = true;
            resetList.add(dtoList.get(num));
            log.error(dtoList.get(0).getHost() + "推送host-mq产生异常",e);
        } finally {
            if (reset) {
                sendScanningHostToMqtt(resetList);
            }
        }
    }

}
