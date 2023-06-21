package com.lhh.serverReScan.mqtt;

import com.lhh.serverReScan.service.ParentDomainService;
import com.lhh.serverbase.dto.ReScanDto;
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
        value = @Queue(value = "reScanDomain", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ParentDomainListener {

    @Autowired
    ParentDomainService parentDomainService;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ReScanDto dto = (ReScanDto) SerializationUtils.deserialize(bytes);
        log.info("开始处理重扫队列" + dto.getQueueId());
        try {
            parentDomainService.scanDomain(dto);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            log.info("队列" + dto.getQueueId() + "处理完毕");
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.error("队列" + dto.getQueueId() + "处理失败", e);
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
