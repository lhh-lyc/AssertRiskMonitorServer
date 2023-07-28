package com.lhh.servermonitor.mqtt;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.dto.ScanParamDto;
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
        value = @Queue(value = "scanningIpData", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ScanningIpListener {

    @Autowired
    SysDictService sysDictService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    ScanHostPortService scanHostPortService;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        /*List<SysDictEntity> dictList = sysDictService.list(new HashMap<String, Object>(){{put("type", "stop_scan_ip");}});
        Integer value = Integer.valueOf(dictList.get(0).getValue());
        Boolean stopFlag = Const.INTEGER_1.equals(value);
        while (stopFlag) {
            try {
                System.out.println("需要重启，开始阻塞...");
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        ScanParamDto dto = (ScanParamDto) SerializationUtils.deserialize(bytes);
        try {
            log.info("扫描ip端口：" + JSON.toJSONString(dto));
            scanPortInfoService.scanIpsPortList(dto);
            scanHostPortService.scanSingleHostPortList(dto.getSubIp());
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
