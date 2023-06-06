package com.lhh.servermonitor.service;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.*;
import com.lhh.serverbase.utils.*;
import com.lhh.servermonitor.mqtt.MqHostSender;
import com.lhh.servermonitor.sync.SyncService;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScanService {

    @Value("${dir-setting.subfinder-dir}")
    private String subfinderDir;

    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    ScanProjectContentService scanProjectContentService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    SyncService syncService;
    @Autowired
    MqHostSender mqHostSender;

    public void scanDomainList2(ScanParamDto scanDto) {
        List<String> subdomainList = new ArrayList<>();
        if (Const.INTEGER_1.equals(scanDto.getSubDomainFlag())) {
            log.info(scanDto.getHost() + "子域名收集");
            // 子域名列表
            String cmd = String.format(Const.STR_SUBFINDER_SUBDOMAIN, subfinderDir, scanDto.getHost());
            SshResponse response = null;
            try {
                response = ExecUtil.runCommand(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            subdomainList = response.getOutList();
            subdomainList = subdomainList.stream().distinct().collect(Collectors.toList());
            log.info(CollectionUtils.isEmpty(subdomainList) ? scanDto.getHost() + "未扫描到子域名" : scanDto.getHost() + "子域名有:" + String.join(Const.STR_COMMA, subdomainList));
        }
        if (!subdomainList.contains(scanDto.getHost())) {
            subdomainList.add(scanDto.getHost());
        }
        List<ScanProjectHostEntity> projectHostList = new ArrayList<>();
        List<ScanParamDto> dtoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(subdomainList)) {
            // 查询域名关联是否已存在，防止服务重启等情况导致一个项目多个相同域名关联
            List<ScanProjectHostEntity> phList = scanProjectHostService.selByProIdAndHost(scanDto.getProjectId(), Const.STR_EMPTY);
            List<String> exitPhList = phList.stream().map(ScanProjectHostEntity::getHost).collect(Collectors.toList());
            for (String subdomain : subdomainList) {
                // 保存项目-host关联关系
                if (!exitPhList.contains(subdomain)) {
                    ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                            .projectId(scanDto.getProjectId())
                            .parentDomain(scanDto.getHost()).host(subdomain)
                            .isScanning(Const.INTEGER_1)
                            .build();
                    projectHostList.add(item);

                    ScanParamDto dto = new ScanParamDto();
                    CopyUtils.copyProperties(scanDto, dto);
                    dto.setSubDomain(subdomain);
                    dtoList.add(dto);
                }
            }
            // todo 增加事务
            scanProjectHostService.saveBatch(projectHostList);
            mqHostSender.sendScanningHostToMqtt(dtoList);
        }
    }

    /**
     * java代码解析子域名ip
     */
    private List<ScanParamDto> getDomainIpList(List<ScanParamDto> dtoList) {
        List<ScanParamDto> ipList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dtoList)) {
            for (ScanParamDto dto : dtoList) {
                List<String> list = new ArrayList<>();
                try {
                    InetAddress[] inetadd = InetAddress.getAllByName(dto.getSubDomain());
                    //遍历所有的ip并输出
                    for (int i = 0; i < inetadd.length; i++) {
                        if (!StringUtils.isEmpty(inetadd[i] + Const.STR_EMPTY)) {
                            String ip = (inetadd[i] + Const.STR_EMPTY).split(Const.STR_SLASH)[1];
                            if (RexpUtil.isIP(ip)) {
                                list.add(ip);
                            }
                        }
                    }
//                    if (CollectionUtils.isEmpty(list)) {
//                        log.info(dto.getSubDomain() + "未解析出ip");
//                        break;
//                    }

                    String ips = CollectionUtils.isEmpty(list) ? Const.STR_EMPTY : String.join(Const.STR_COMMA, list);
                    log.info(dto.getSubDomain() + (CollectionUtils.isEmpty(list) ? "未解析出ip" : "解析ip为：" + ips));
                    dto.setSubIpList(list);
                } catch (UnknownHostException e) {
                    list.add(Const.STR_CROSSBAR);
                    dto.setSubIpList(list);
                    log.error(dto.getSubDomain() + "解析ip出错");
                }
                ipList.add(dto);
            }
        }
        return ipList;
    }

}
