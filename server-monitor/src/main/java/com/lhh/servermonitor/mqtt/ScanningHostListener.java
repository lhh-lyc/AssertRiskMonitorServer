package com.lhh.servermonitor.mqtt;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.service.*;
import com.lhh.servermonitor.utils.JedisUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(value = "scanningHostData", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ScanningHostListener {

    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanPortService scanPortService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    ScanHostPortService scanHostPortService;
    @Autowired
    ScanHoleService scanHoleService;
    @Autowired
    ScanProjectContentService scanProjectContentService;
    @Autowired
    HostCompanyService hostCompanyService;
    @Autowired
    MqIpSender mqIpSender;
    @Autowired
    RedissonClient redisson;
    @Autowired
    RedisLock redisLock;
    @Autowired
    TmpRedisService tmpRedisService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ScanParamDto dto = (ScanParamDto) SerializationUtils.deserialize(bytes);
        // todo bug:好像没做端口不同扫描的处理，不同的端口直接跳过去了
        //  解决办法：1.content表保留scan_ports字段
        //  2.host_company表存一个预先的端口字段，新建任务就更新，存一个已完成的端口字段，每个子域名扫描完再更新，
        //  预先端口用于刚开始过滤已扫描主域名，已完成端口用于中间过程端口处理
//        String lockKey = String.format(CacheConst.REDIS_LOCK_SUBDOMAIN, dto.getSubDomain());
//        RLock lock = redisson.getLock(lockKey);
//        boolean success = true;
//        try {
//            success = lock.tryLock(1, TimeUnit.SECONDS);
//            if (success) {
                deal(dto, message, channel);
//            }
//        } catch (Exception e) {} finally {
//            // 判断当前线程是否持有锁
//            if (success && lock.isHeldByCurrentThread()) {
//                //释放当前锁
//                lock.unlock();
//            }
//        }
    }

    public void deal(ScanParamDto dto, Message message, Channel channel){
        try {
            log.info("开始处理项目" + dto.getProjectId() + "域名：" + dto.getSubDomain());
            String ports = tmpRedisService.getDomainScanPorts(dto.getHost());
            if (PortUtils.portEquals(ports, dto.getScanPorts())) {
                redisLock.delDomainRedis(dto.getProjectId(), dto.getHost(), dto.getSubDomain(), dto.getScanPorts());
                log.info(dto.getSubDomain() + "该子域名的主域名已全部扫描完毕!");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                return;
            }
            List<String> ipList = dto.getSubIpList();
            List<Long> ipLongList = ipList.stream().map(s -> IpLongUtils.ipToLong(s)).collect(Collectors.toList());

            String projectStr = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_PROJECT, dto.getProjectId()));
            ScanProjectEntity redisProject = JSON.parseObject(projectStr, ScanProjectEntity.class);
            String parentDomain = RexpUtil.getMajorDomain(dto.getHost());
            String company = hostCompanyService.getCompany(parentDomain);
            List<ScanHostEntity> exitIpEntityList = scanHostService.getByIpList(ipLongList, dto.getSubDomain());
            List<Long> exitIpList = exitIpEntityList.stream().map(ScanHostEntity::getIpLong).collect(Collectors.toList());
            List<ScanParamDto> scanPortParamList = new ArrayList<>();
            List<ScanHostEntity> saveHostList = new ArrayList<>();
            String scanPorts = PortUtils.getNewPorts(ports, dto.getScanPorts());
            String allPorts = PortUtils.getAllPorts(ports, dto.getScanPorts());
            if (!CollectionUtils.isEmpty(ipList)) {
                for (String ip : ipList) {
                    // 扫描端口ip
                    if (Const.INTEGER_1.equals(dto.getPortFlag())) {
                        ScanParamDto ipDto = ScanParamDto.builder()
                                .host(parentDomain).subDomain(dto.getSubDomain())
                                .subIp(ip).scanPorts(scanPorts).allPorts(allPorts)
                                .portTool(redisProject == null ? Const.INTEGER_1 : redisProject.getPortTool())
                                .build();
                        scanPortParamList.add(ipDto);
                    }
                    // 新的域名与ip组合
                    Long ipLong = IpLongUtils.ipToLong(ip);
                    if (CollectionUtils.isEmpty(exitIpList) || !exitIpList.contains(ipLong)) {
                        ScanHostEntity host = ScanHostEntity.builder()
                                .parentDomain(parentDomain)
                                .domain(dto.getSubDomain())
                                .ip(ip).ipLong(ipLong)
                                .company(StringUtils.isEmpty(company) ? Const.STR_CROSSBAR : company)
                                .type(Const.INTEGER_3)
                                .isMajor(RexpUtil.isMajorDomain(dto.getSubDomain()) ? Const.INTEGER_1 : Const.INTEGER_0)
                                .isDomain(Const.INTEGER_1)
                                .isScanning(Const.INTEGER_1)
                                .subIpList(ipList)
                                .build();
                        saveHostList.add(host);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(saveHostList)) {
                scanHostService.saveBatch(saveHostList);
            }
            scanPortParamList.stream().forEach(d -> {
                //业务处理
                try {
                    scanPortInfoService.scanSingleIpPortList(d);
                } catch (Exception e) {
                    log.info("扫描端口异常", e);
                }
            });

            // 扫描url、title、cms
            scanHostPortService.scanSingleHostPortList(dto.getSubDomain());
            scanHoleService.scanHoleList(dto.getProjectId(), dto.getSubDomain());
            redisLock.delDomainRedis(dto.getProjectId(), dto.getHost(), dto.getSubDomain(), dto.getScanPorts());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.error("子域名:" + dto.getSubDomain() + "处理异常", e);
            } catch (IOException ioException) {
                log.error("产生异常的参数", ioException);
            }
        }
    }

}
