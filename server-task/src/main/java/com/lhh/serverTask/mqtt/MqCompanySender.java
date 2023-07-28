package com.lhh.serverTask.mqtt;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serverbase.common.constant.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MqCompanySender {

    @Value("${mqtt-setting.exchange}")
    private String exchange;
    @Value("${mqtt-setting.taskCompany-route-key}")
    private String companyRouteKey;
    @Autowired
    AmqpTemplate amqpTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendCompanyToMqtt(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Boolean reset = false;
        List<String> resetList = new ArrayList<>();
        Integer num = Const.INTEGER_0;
        for (String s : list) {
            try {
                num++;
                CorrelationData correlationId = new CorrelationData(s);
                rabbitTemplate.convertAndSend(exchange, companyRouteKey, s, correlationId);
            } catch (Exception e) {
                reset = true;
                resetList.add(list.get(num));
                log.error(list.get(num) + "推送company-mq产生异常");
            }
        }
        if (reset) {
            sendCompanyToMqtt(resetList);
        }
    }

}
