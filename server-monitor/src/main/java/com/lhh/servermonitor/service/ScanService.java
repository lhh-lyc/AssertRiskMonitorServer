package com.lhh.servermonitor.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanHostIpEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.CacheConst;
import com.lhh.serverbase.utils.Const;
import com.lhh.servermonitor.dto.ScanParamDto;
import com.lhh.servermonitor.enums.OperateEnum;
import com.lhh.servermonitor.utils.CopyUtils;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.JedisUtils;
import com.lhh.servermonitor.utils.RexpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScanService {

    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanHostIpService scanHostIpService;
    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    ScanPortInfoService scanPortInfoService;

    public void scanDomainList(ScanParamDto scanDto) {
        log.info(scanDto.getHost() + "子域名收集");
        // 子域名列表
        String cmd = String.format(Const.STR_SUBFINDER_SUBDOMAIN, scanDto.getHost());
        SshResponse response = null;
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> subdomainList = response.getOutList();
        Map<String, String> redisMap = new HashMap<>();
        List<ScanParamDto> dtoList = new ArrayList<>();
        log.info(CollectionUtils.isEmpty(subdomainList) ? scanDto.getHost() + "未扫描到子域名" : scanDto.getHost() + "子域名有:" + String.join(Const.STR_COMMA, subdomainList));
        subdomainList.add(scanDto.getHost());
        if (!CollectionUtils.isEmpty(subdomainList)) {
            List<String> exitList = subdomainList.stream().filter(i -> JedisUtils.exists(String.format(CacheConst.REDIS_IP_INFO, i)))
                    .map(i -> String.format(CacheConst.REDIS_IP_INFO, i)).collect(Collectors.toList());
            Map<String, String> exitInfoList = JedisUtils.getPipeJson(exitList);
            for (String subdomain : subdomainList) {
                // 已缓存域名跳过
                String key = String.format(CacheConst.REDIS_IP_INFO, subdomain);
                String value = exitInfoList.get(key);
                if (!StringUtils.isEmpty(value)) {
                    JSONObject obj = JSONObject.parseObject(value);
                    ScanHostEntity host = scanHostService.getByHost(subdomain);
                    // 输入域名子域名循环，当不等于输入域名域名且mysql父域名等于该域名时，说明是子域名已存在mysql并该更新父域名了(同理redis)
                    if (!scanDto.getHost().equals(subdomain) && host.getParentHost().equals(subdomain)) {
                        host.setParentHost(scanDto.getHost());
                        scanHostService.updateById(host);
                        obj.put("parentDomain", scanDto.getHost());
                        redisMap.put(key, JSON.toJSONString(obj));
                    }
                    continue;
                }
                ScanParamDto dto = new ScanParamDto();
                CopyUtils.copyProperties(scanDto, dto);
                dto.setParentDomain(scanDto.getHost());
                dto.setHost(subdomain);
                dtoList.add(dto);

                redisMap.put(String.format(CacheConst.REDIS_IP_INFO, subdomain), JSON.toJSONString(dto));
            }
            if (!CollectionUtils.isEmpty(redisMap)) {
                JedisUtils.setPipeJson(redisMap);
                redisMap.clear();
            }

            // 子域名解析ip
            List<ScanHostEntity> hostList = new ArrayList<>();
            List<ScanProjectHostEntity> projectHostList = new ArrayList<>();
            List<ScanHostIpEntity> hostIpList = new ArrayList<>();
            List<ScanParamDto> ipList = getDomainIpList(dtoList);
            List<ScanParamDto> scanPortParamList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(ipList)) {
                String ports = ipList.get(0).getPorts();
                for (ScanParamDto sub : ipList) {
                    ScanHostEntity host = ScanHostEntity.builder()
                            .host(sub.getHost())
                            .parentHost(sub.getHost())
                            .type(Const.INTEGER_3)
                            .subIpList(sub.getSubIpList())
                            .build();
                    hostList.add(host);

                    // 保存项目-host关联关系
                    ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                            .projectId(scanDto.getProjectId()).host(sub.getHost())
                            .build();
                    projectHostList.add(item);

                    // 保存域名-ip关联关系
                    if (!CollectionUtils.isEmpty(sub.getSubIpList())) {
                        for (String ip : sub.getSubIpList()) {
                            ScanHostIpEntity scanHostIp = ScanHostIpEntity.builder()
                                    .host(sub.getHost()).ip(ip)
                                    .build();
                            hostIpList.add(scanHostIp);
                            redisMap.put(String.format(CacheConst.REDIS_IP_INFO, ip), JSON.toJSONString(scanHostIp));
                        }
                        hostIpList = hostIpList.stream().distinct().collect(Collectors.toList());
                    }
                    ScanParamDto dto = ScanParamDto.builder()
                            .subDomain(host.getHost())
                            .subIpList(host.getSubIpList()).ports(ports)
                            .build();
                    scanPortParamList.add(dto);
                }
                JedisUtils.setPipeJson(redisMap);
                scanHostService.saveBatch(hostList);
                scanHostIpService.saveBatch(hostIpList);
                scanProjectHostService.saveBatch(projectHostList);
            }

            if (!CollectionUtils.isEmpty(scanPortParamList)) {
                for (ScanParamDto param : scanPortParamList) {
                    scanPortInfoService.scanPortList(param);
                }
            }
        }
    }

    /**
     * java代码解析子域名ip
     */
    public List<ScanParamDto> getDomainIpList(List<ScanParamDto> dtoList) {
        List<ScanParamDto> ipList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dtoList)) {
            for (ScanParamDto dto : dtoList) {
                try {
                    List<String> list = new ArrayList<>();
                    InetAddress[] inetadd = InetAddress.getAllByName(dto.getHost());
                    //遍历所有的ip并输出
                    for (int i = 0; i < inetadd.length; i++) {
                        if (!StringUtils.isEmpty(inetadd[i] + Const.STR_EMPTY)) {
                            String ip = (inetadd[i] + Const.STR_EMPTY).split(Const.STR_SLASH)[1];
                            if (RexpUtil.isIP(ip)) {
                                list.add(ip);
                            }
                        }
                    }
                    if (CollectionUtils.isEmpty(list)) {
                        log.info(dto.getHost() + "未解析出ip");
                        break;
                    }
                    String ips = String.join(Const.STR_COMMA, list);
                    log.info(dto.getHost() + "解析ip为：" + ips);
                    dto.setSubIpList(list);
                    ipList.add(dto);
                } catch (UnknownHostException e) {
                    log.error(dto.getSubDomain() + "解析ip出错");
                }
            }
        }
        return ipList;
    }

}
