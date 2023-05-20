package com.lhh.servermonitor.mqtt;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.*;
import com.lhh.servermonitor.service.ScanHostService;
import com.lhh.servermonitor.sync.SyncService;
import com.lhh.servermonitor.utils.JedisUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(value = "scanningHostData", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ScanningHostListener {

    @Autowired
    SyncService syncService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    MqIpSender mqIpSender;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ScanParamDto dto = (ScanParamDto) SerializationUtils.deserialize(bytes);
        log.info("开始处理项目" + dto.getProjectId() + "域名：" + dto.getSubDomain());
        List<String> ipList = getDomainIpList(dto.getSubDomain());
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
        String company = HttpUtils.getDomainUnit(dto.getSubDomain());
        List<ScanHostEntity> exitIpInfoList = scanHostService.getByIpList(ipList);
        Map<String, List<ScanHostEntity>> ipMap = exitIpInfoList.stream().collect(Collectors.groupingBy(h->h.getIp() + Const.STR_UNDERLINE + h.getDomain()));
        List<ScanParamDto> scanPortParamList = new ArrayList<>();
        List<ScanHostEntity> saveHostList = new ArrayList<>();
        List<ScanHostEntity> updateHostList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ipList)) {
            for (String ip : ipList) {
                String scanPorts = ipPortsMap.get(ip + Const.STR_UNDERLINE + dto.getSubDomain());
                List<ScanHostEntity> exitIpList = ipMap.get(ip + Const.STR_UNDERLINE + dto.getSubDomain());
                // 扫描端口ip
                if (Const.INTEGER_1.equals(dto.getPortFlag())) {
                    ScanParamDto ipDto = ScanParamDto.builder()
                            .subIp(ip).scanPorts(dto.getScanPorts())
                            .build();
                    scanPortParamList.add(ipDto);

                    // 更新域名扫描端口
                    if (!CollectionUtils.isEmpty(exitIpList)) {
                        for (ScanHostEntity host : exitIpList) {
                            if (!PortUtils.portEquals(host.getScanPorts(), dto.getScanPorts())) {
                                host.setScanPorts(PortUtils.getNewPorts(host.getScanPorts(), scanPorts));
                            }
                        }
                        updateHostList.addAll(exitIpList);
                    }
                }
                // 新的域名与ip组合
                if (CollectionUtils.isEmpty(exitIpList)) {
                    ScanHostEntity host = ScanHostEntity.builder()
                            .parentDomain(parentDomain)
                            .domain(dto.getSubDomain())
                            .ip(ip).ipLong(IpLongUtils.ipToLong(ip)).scanPorts(scanPorts)
                            .company(company)
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
        if (!CollectionUtils.isEmpty(updateHostList)) {
            // todo
//                scanHostService.updateScanPorts(updateHostList);
            for (ScanHostEntity host : updateHostList) {
                scanHostService.updateById(host);
            }
        }
        mqIpSender.sendScanningIpToMqtt(ScanParamDto.builder().dtoList(scanPortParamList).build());
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * java代码解析子域名ip
     */
    private List<String> getDomainIpList(String domain) {
        List<String> list = new ArrayList<>();
        try {
            InetAddress[] inetadd = InetAddress.getAllByName(domain);
            //遍历所有的ip并输出
            for (int i = 0; i < inetadd.length; i++) {
                if (!StringUtils.isEmpty(inetadd[i] + Const.STR_EMPTY)) {
                    String ip = (inetadd[i] + Const.STR_EMPTY).split(Const.STR_SLASH)[1];
                    if (RexpUtil.isIP(ip)) {
                        list.add(ip);
                    }
                }
            }
            String ips = CollectionUtils.isEmpty(list) ? Const.STR_EMPTY : String.join(Const.STR_COMMA, list);
            log.info(domain + (CollectionUtils.isEmpty(list) ? "未解析出ip" : "解析ip为：" + ips));
        } catch (UnknownHostException e) {
            list.add(Const.STR_CROSSBAR);
            log.error(domain + "解析ip出错");
        }
        return list;
    }

}
