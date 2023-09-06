package com.lhh.serveradmin.mqtt;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serveradmin.config.RabbitMqConfig;
import com.lhh.serveradmin.feign.scan.HostCompanyFeign;
import com.lhh.serveradmin.utils.JedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ReScanDto;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.utils.CopyUtils;
import com.lhh.serverbase.utils.HttpUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.serverbase.utils.RexpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProjectSender {

    @Value("${mqtt-setting.sub-num}")
    private Integer subNum;
    @Value("${mqtt-setting.exchange}")
    private String exchange;
    @Value("${mqtt-setting.project-route-key}")
    private String projectRouteKey;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private HostCompanyFeign hostCompanyFeign;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Async
    public void putProject(ScanProjectEntity project) {
        if (CollectionUtils.isEmpty(project.getHostList())) {
            return;
        }
        List<HostCompanyEntity> hostInfoList = new ArrayList<>();
        List<HostCompanyEntity> oldList = hostCompanyFeign.list(new HashMap<String, Object>(){{put("hostList", project.getHostList());}});
        Map<String, HostCompanyEntity> oldMap = oldList.stream().collect(Collectors.toMap(HostCompanyEntity::getHost, host -> host));
        for (String host : project.getHostList()) {
            if (!oldMap.containsKey(host)) {
                String parentDomain = RexpUtil.getMajorDomain(host);
                String company = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_DOMAIN_COMPANY, parentDomain));
                HostCompanyEntity saveCompany = HostCompanyEntity.builder()
                        .host(parentDomain).company(company)
                        .build();
                hostInfoList.add(saveCompany);
            }
        }
        if (!CollectionUtils.isEmpty(hostInfoList)) {
            hostCompanyFeign.saveBatch(hostInfoList);
        }

        for (String host : project.getHostList()) {
            String parentDomain = RexpUtil.getMajorDomain(host);
            if (!JedisUtils.exists(String.format(CacheConst.REDIS_DOMAIN_COMPANY, parentDomain))) {
                String company = HttpUtils.getDomainUnit(parentDomain);
                company = StringUtils.isEmpty(company) ? Const.STR_CROSSBAR : company;
                HostCompanyEntity companyEntity = hostCompanyFeign.queryBasicInfo(parentDomain);
                companyEntity.setCompany(company);
                hostCompanyFeign.update(companyEntity);
                JedisUtils.setJson(String.format(CacheConst.REDIS_DOMAIN_COMPANY, parentDomain), company);
//                JedisUtils.setJson(String.format(CacheConst.REDIS_DOMAIN_COMPANY, host), company, 60*60*24*7);
            }
        }
        sendToMqtt(project);
    }

    public void sendToMqtt(ScanProjectEntity project) {
        List<ScanProjectEntity> list = splitList(project, subNum);
        for (ScanProjectEntity p : list) {
            CorrelationData correlationId = new CorrelationData(p.toString());
            //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
            rabbitTemplate.convertAndSend(exchange, projectRouteKey, SerializationUtils.serialize(p), correlationId);
        }
    }

    public static List<ScanProjectEntity> splitList(ScanProjectEntity project, int len) {
        List<String> list = project.getHostList();
        if (list == null || list.isEmpty() || len < 1) {
            return Collections.emptyList();
        }
        List<ScanProjectEntity> result = new ArrayList<>();
        int size = list.size();
        int count = (size + len - 1) / len;
        for (int i = 0; i < count; i++) {
            ScanProjectEntity subProject = new ScanProjectEntity();
            CopyUtils.copyProperties(project, subProject);
            subProject.setQueueId(project.getId() + Const.STR_UNDERLINE + count + Const.STR_UNDERLINE + (i + 1));
            List<String> subList = list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)));
            subProject.setHostList(new ArrayList<>(subList));
            result.add(subProject);
        }
        return result;
    }

}
