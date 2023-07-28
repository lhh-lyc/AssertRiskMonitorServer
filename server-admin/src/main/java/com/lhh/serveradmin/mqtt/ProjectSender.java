package com.lhh.serveradmin.mqtt;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serveradmin.config.RabbitMqConfig;
import com.lhh.serveradmin.feign.scan.HostCompanyFeign;
import com.lhh.serveradmin.utils.JedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.utils.CopyUtils;
import com.lhh.serverbase.utils.HttpUtils;
import com.lhh.serverbase.utils.RexpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Async
    public void putProject(ScanProjectEntity project) {
        if (CollectionUtils.isEmpty(project.getHostList())) {
            return;
        }
        List<ScanProjectEntity> list = splitList(project, subNum);
        for (ScanProjectEntity p : list) {
            List<HostCompanyEntity> companyList = new ArrayList<>();
            for (String host : p.getHostList()) {
                if (!JedisUtils.exists(String.format(CacheConst.REDIS_DOMAIN_COMPANY, host))) {
                    String company = HttpUtils.getDomainUnit(host);
                    company = StringUtils.isEmpty(company) ? Const.STR_CROSSBAR : company;
                    HostCompanyEntity companyEntity = HostCompanyEntity.builder()
                            .host(host).company(company)
                            .build();
                    companyList.add(companyEntity);
                    JedisUtils.setJson(String.format(CacheConst.REDIS_DOMAIN_COMPANY, host), company, 60*60*24*7);
                }
            }
            hostCompanyFeign.saveBatch(companyList);
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
