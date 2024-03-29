package com.lhh.serverReScan.mqtt;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ReScanDto;
import com.lhh.serverbase.dto.ScanParamDto;
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
public class MqHostSender {

    @Value("${mqtt-setting.re-sub-num}")
    private Integer reSubNum;
    @Value("${mqtt-setting.exchange}")
    private String exchange;
    @Value("${mqtt-setting.reSubDomain-route-key}")
    private String reSubDomainRouteKey;

    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendReScanHostToMqtt(String host, List<String> domainList) {
        if (CollectionUtils.isEmpty(domainList)) {
            return;
        }
        List<ReScanDto> hostList = splitList(host, domainList, reSubNum);
        try {
            for (ReScanDto dto : hostList) {
                log.info(host + "域名开始投递");
                CorrelationData correlationId = new CorrelationData(dto.toString());
                rabbitTemplate.convertAndSend(exchange, reSubDomainRouteKey, SerializationUtils.serialize(dto), correlationId);
            }
        } catch (Exception e) {
            log.error(host + "推送host-mq产生异常",e);
        }
    }

    public static List<ReScanDto> splitList(String host, List<String> list, int len) {
        List<ReScanDto> result = new ArrayList<>();
        int size = list.size();
        int count = (size + len - 1) / len;
        for (int i = 0; i < count; i++) {
            ReScanDto dto = ReScanDto.builder()
                    .parentDomain(host)
                    .hostList(new ArrayList<>(list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)))))
                    .build();
            result.add(dto);
        }
        return result;
    }

}
