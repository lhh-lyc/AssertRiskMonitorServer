package com.lhh.serveradmin.mqtt;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ReScanDto;
import com.lhh.serverbase.entity.HostCompanyEntity;
import lombok.extern.slf4j.Slf4j;
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
public class ReScanSender {

    @Value("${mqtt-setting.re-sub-num}")
    private Integer reSubNum;
    @Value("${mqtt-setting.exchange}")
    private String exchange;
    @Value("${mqtt-setting.reDomain-route-key}")
    private String reDomainRouteKey;
    @Value("${mqtt-setting.reIp-route-key}")
    private String reIpRouteKey;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void reScanDomainToMqtt(List<HostCompanyEntity> list, String uuid) {
        if (CollectionUtils.isEmpty(list)) {
            return ;
        }
        List<ReScanDto> hostList = splitList(list, uuid, reSubNum);
        for (ReScanDto p : hostList) {
            CorrelationData correlationId = new CorrelationData(p.toString());
            rabbitTemplate.convertAndSend(exchange, reDomainRouteKey, SerializationUtils.serialize(p), correlationId);
        }
    }

    public void reScanIpToMqtt(List<HostCompanyEntity> list, String uuid) {
        if (CollectionUtils.isEmpty(list)) {
            return ;
        }
        List<ReScanDto> hostList = splitList(list, uuid, reSubNum);
        for (ReScanDto p : hostList) {
            CorrelationData correlationId = new CorrelationData(p.toString());
            rabbitTemplate.convertAndSend(exchange, reIpRouteKey, SerializationUtils.serialize(p), correlationId);
        }
    }

    public static List<ReScanDto> splitList(List<HostCompanyEntity> list, String uuid, int len) {
        List<ReScanDto> result = new ArrayList<>();
        int size = list.size();
        int count = (size + len - 1) / len;
        for (int i = 0; i < count; i++) {
            ReScanDto dto = ReScanDto.builder()
                    .uuid(uuid)
                    .queueId("重扫队列" + Const.STR_UNDERLINE + count + Const.STR_UNDERLINE + (i+1))
                    .parentDomainList(new ArrayList<>(list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)))))
                    .build();
            result.add(dto);
        }
        return result;
    }

}
