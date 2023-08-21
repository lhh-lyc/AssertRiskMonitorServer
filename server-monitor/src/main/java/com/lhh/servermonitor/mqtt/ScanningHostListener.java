package com.lhh.servermonitor.mqtt;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
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
    ScanProjectContentService scanProjectContentService;
    @Autowired
    MqIpSender mqIpSender;
    @Autowired
    RedissonClient redisson;
    @Autowired
    RedisLock redisLock;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ScanParamDto dto = (ScanParamDto) SerializationUtils.deserialize(bytes);
        String lockKey = String.format(CacheConst.REDIS_LOCK_SUBDOMAIN, dto.getSubDomain());
        RLock lock = redisson.getLock(lockKey);
        boolean success = true;
        try {
            success = lock.tryLock(1, TimeUnit.SECONDS);
            if (success) {
                deal(dto, message, channel);
            }
        } catch (Exception e) {} finally {
            // 判断当前线程是否持有锁
            if (success && lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
    }

    public void deal(ScanParamDto dto, Message message, Channel channel){
        try {
            log.info("开始处理项目" + dto.getProjectId() + "域名：" + dto.getSubDomain());
            List<String> ipList = dto.getSubIpList();
            List<Long> ipLongList = ipList.stream().map(s -> IpLongUtils.ipToLong(s)).collect(Collectors.toList());
            Map<String, String> redisMap = new HashMap<>();
            Map<String, String> ipPortsMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(ipList)) {
                for (String ip : ipList) {
                    // 解决相同ip扫描不同端口，多线程同时修改scan_ports字段问题
                    // map存储了此线程ports和所有正在其他更改的ports的交集
                    JSONObject obj = JSONObject.parseObject(JedisUtils.getStr(String.format(CacheConst.REDIS_SCANNING_IP, ip)));
                    String ipScanPorts = obj == null || obj.get("ports") == null ? Const.STR_EMPTY : MapUtil.getStr(obj, "ports");
                    String newIpScanPorts = StringUtils.isEmpty(ipScanPorts) ? dto.getScanPorts() : PortUtils.getNewPorts(ipScanPorts, dto.getScanPorts());
                    ipPortsMap.put(ip + Const.STR_UNDERLINE + dto.getSubDomain(), newIpScanPorts);
                    // 扫描端口
                    if (Const.INTEGER_1.equals(dto.getPortFlag())) {
                        Map<String, String> ipMap = new HashMap<>();
                        ipMap.put("ports", newIpScanPorts);
                        ipMap.put("status", Const.STR_0);
                        redisMap.put(String.format(CacheConst.REDIS_SCANNING_IP, ip), JSON.toJSONString(ipMap));
                    }
                }
            }
            if (!CollectionUtils.isEmpty(redisMap)) {
                JedisUtils.setPipeJson(redisMap);
            }

            String parentDomain = RexpUtil.getMajorDomain(dto.getHost());
            String company = JedisUtils.getStr(String.format(CacheConst.REDIS_DOMAIN_COMPANY, parentDomain));
            List<ScanHostEntity> exitIpList = scanHostService.getByIpList(ipLongList, dto.getSubDomain());
            List<ScanParamDto> scanPortParamList = new ArrayList<>();
            List<ScanHostEntity> saveHostList = new ArrayList<>();
            List<Long> updateIpList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(ipList)) {
                for (String ip : ipList) {
                    String scanPorts = ipPortsMap.get(ip + Const.STR_UNDERLINE + dto.getSubDomain());
                    // 扫描端口ip
                    if (Const.INTEGER_1.equals(dto.getPortFlag())) {
                        ScanParamDto ipDto = ScanParamDto.builder()
                                .subDomain(dto.getSubDomain())
                                .subIp(ip).scanPorts(scanPorts)
                                .build();
                        scanPortParamList.add(ipDto);

                        // 已有域名ip，端口范围不一样时，重新修改为正在扫描状态
                        /*if (!CollectionUtils.isEmpty(exitIpList)) {
                            for (ScanHostEntity host : exitIpList) {
                                if (!PortUtils.portEquals(host.getScanPorts(), dto.getScanPorts())) {
                                    updateIpList.add(host.getIpLong());
                                }
                            }
                        }*/
                    }
                    // 新的域名与ip组合
                    if (CollectionUtils.isEmpty(exitIpList)) {
                        ScanHostEntity host = ScanHostEntity.builder()
                                .parentDomain(parentDomain)
                                .domain(dto.getSubDomain())
                                .ip(ip).ipLong(IpLongUtils.ipToLong(ip)).scanPorts(scanPorts)
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
            /*if (!CollectionUtils.isEmpty(updateIpList)) {
                for (Long ip : updateIpList) {
                    scanHostService.returnScanStatus(ip);
                }
            }*/
            scanPortParamList.stream().forEach(d -> {
                //业务处理
                scanPortInfoService.scanSingleIpPortList(d);
            });

            scanHostPortService.scanSingleHostPortList(dto.getSubDomain());

//            scanProjectHostService.updateEndScanDomain(dto.getSubDomain());
            // 不扫描端口批量更新域名ip状态
            /*if (!Const.INTEGER_1.equals(dto.getPortFlag())) {
                try {
                    scanHostService.updateEndScanDomain(dto.getSubDomain());
                } catch (Exception e) {
                    log.error(dto.getSubDomain() + "批量更新状态出现错误：", e);
                }
            }*/
            List<Long> projectIdList = scanProjectContentService.getProjectIdList(dto.getHost());
            redisLock.delDomainRedis(projectIdList, dto.getHost(), dto.getSubDomain());
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
