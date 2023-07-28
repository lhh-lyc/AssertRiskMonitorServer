package com.lhh.serverTask.mqtt;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serverTask.utils.RedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ReScanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ParentDomainSender {

    @Value("${mqtt-setting.task-sub-num}")
    private Integer reSubNum;
    @Value("${mqtt-setting.exchange}")
    private String exchange;
    @Value("${mqtt-setting.taskParent-route-key}")
    private String taskParentRouteKey;

    @Autowired
    AmqpTemplate amqpTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendParentToMqtt(List<String> domainList) {
        if (CollectionUtils.isEmpty(domainList)) {
            return;
        }
        List<ReScanDto> hostList = splitList(domainList, reSubNum);
        try {
            for (ReScanDto dto : hostList) {
                // 如意外故障不重复推
                Boolean flg = true;
                Map<String, String> map = new HashMap<>();
                for (String h : dto.getHostList()) {
                    if (stringRedisTemplate.hasKey(String.format(CacheConst.REDIS_TASK_PARENT_DOMAIN, h))) {
                        flg = false;
                        break;
                    }
                    map.put(String.format(CacheConst.REDIS_TASK_PARENT_DOMAIN, h), h);
                }
                if (flg) {
                    log.info("域名开始投递：" + JSON.toJSONString(dto));
                    CorrelationData correlationId = new CorrelationData(dto.toString());
                    rabbitTemplate.convertAndSend(exchange, taskParentRouteKey, SerializationUtils.serialize(dto), correlationId);
                    redisUtils.addStringBatch(map);
                }
            }
        } catch (Exception e) {
            log.error("推送host-mq产生异常",e);
        }
    }

    public static List<ReScanDto> splitList(List<String> list, int len) {
        List<ReScanDto> result = new ArrayList<>();
        int size = list.size();
        int count = (size + len - 1) / len;
        for (int i = 0; i < count; i++) {
            ReScanDto dto = ReScanDto.builder()
                    .hostList(new ArrayList<>(list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)))))
                    .queueId(count + Const.STR_UNDERLINE + (i+1))
                    .build();
            result.add(dto);
        }
        return result;
    }

}
