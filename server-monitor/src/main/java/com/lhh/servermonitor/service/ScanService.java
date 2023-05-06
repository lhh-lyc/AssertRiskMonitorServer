package com.lhh.servermonitor.service;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.CopyUtils;
import com.lhh.serverbase.utils.HttpUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.serverbase.utils.RexpUtil;
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

    public void scanDomainList(ScanParamDto scanDto) {
        log.info("8");
        String parentDomain = RexpUtil.getMajorDomain(scanDto.getHost());
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
        List<ScanParamDto> dtoList = new ArrayList<>();
        if (!subdomainList.contains(scanDto.getHost())) {
            subdomainList.add(scanDto.getHost());
        }
        log.info("9");
        List<ScanHostEntity> saveHostList = new ArrayList<>();
        List<ScanHostEntity> updateHostList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(subdomainList)) {
//            List<ScanHostEntity> exitHostInfoList = scanHostService.getByDomainList(subdomainList);
//            Map<String, ScanHostEntity> hostMap = exitHostInfoList.stream().collect(Collectors.toMap(ScanHostEntity::getDomain, Function.identity(), (key1, key2) -> key2));
            for (String subdomain : subdomainList) {
                /*ScanHostEntity host = hostMap.get(subdomain);
                if (host != null) {
                    if (PortUtils.portEquals(host.getScanPorts(), scanDto.getScanPorts())) {
                        // 已扫描域名跳过
                        // 子域名循环，当子域名不等于输入域名域名且mysql父域名等于该域名时，说明是子域名已存在mysql并该更新父域名了
                        if (!scanDto.getHost().equals(subdomain) && host.getParentDomain().equals(subdomain)) {
                            host.setParentDomain(scanDto.getHost());
                            updateHostList.add(host);
                        }
                        continue;
                    }
                }*/
                ScanParamDto dto = new ScanParamDto();
                CopyUtils.copyProperties(scanDto, dto);
                dto.setSubDomain(subdomain);
                dtoList.add(dto);
            }

            // 子域名解析ip
            log.info("10");
            Map<String, String> redisMap = new HashMap<>();
            List<ScanProjectHostEntity> projectHostList = new ArrayList<>();
            List<ScanParamDto> ipInfoList = getDomainIpList(dtoList);
            Map<String, String> ipPortsMap = new HashMap<>();
            List<ScanParamDto> scanPortParamList = new ArrayList<>();
            List<String> ipList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(ipInfoList)) {
                for (ScanParamDto dto : ipInfoList) {
                    if (!CollectionUtils.isEmpty(dto.getSubIpList())) {
                        for (String ip : dto.getSubIpList()) {
                            ipList.add(ip);
                            // 解决相同ip扫描不同端口，多线程同时修改scan_ports字段问题
                            // map存储了此线程ports和所有正在其他更改的ports的交集
                            JSONObject obj = JSONObject.parseObject(JedisUtils.getStr(String.format(CacheConst.REDIS_SCANNING_IP, ip)));
                            String ipScanPorts = obj == null || obj.get("ports") == null ? Const.STR_EMPTY : MapUtil.getStr(obj, "ports");
                            String newIpScanPorts = StringUtils.isEmpty(ipScanPorts) ? dto.getScanPorts() : PortUtils.getNewPorts(ipScanPorts, dto.getScanPorts());
                            ipPortsMap.put(ip + Const.STR_UNDERLINE + dto.getSubDomain(), newIpScanPorts);
                            // 扫描端口
                            if (Const.INTEGER_1.equals(scanDto.getPortFlag())) {
                                Map<String, String> ipMap = new HashMap<>();
                                ipMap.put("ports", newIpScanPorts);
                                ipMap.put("status", Const.STR_0);
                                redisMap.put(String.format(CacheConst.REDIS_SCANNING_IP, ip), JSON.toJSONString(ipMap));
                            }
                        }
                    }
                }
                if (!CollectionUtils.isEmpty(redisMap)) {
                    JedisUtils.setPipeJson(redisMap);
                }

                List<ScanProjectHostEntity> phList = scanProjectHostService.selByProIdAndHost(scanDto.getProjectId(), Const.STR_EMPTY);
                List<String> exitPhList = phList.stream().map(ScanProjectHostEntity::getHost).collect(Collectors.toList());
                List<ScanHostEntity> exitIpInfoList = scanHostService.getByIpList(ipList);
                Map<String, List<ScanHostEntity>> ipMap = exitIpInfoList.stream().collect(Collectors.groupingBy(h->h.getIp() + Const.STR_UNDERLINE + h.getDomain()));
                String company = HttpUtils.getDomainUnit(scanDto.getHost());
                for (ScanParamDto sub : ipInfoList) {
                    if (!CollectionUtils.isEmpty(sub.getSubIpList())) {
                        for (String ip : sub.getSubIpList()) {
                            String scanPorts = ipPortsMap.get(ip + Const.STR_UNDERLINE + sub.getSubDomain());
                            List<ScanHostEntity> exitIpList = ipMap.get(ip + Const.STR_UNDERLINE + sub.getSubDomain());
                            // 扫描端口
                            if (Const.INTEGER_1.equals(scanDto.getPortFlag())) {
                                ScanParamDto dto = ScanParamDto.builder()
                                        .subIp(ip).scanPorts(scanDto.getScanPorts())
                                        .build();
                                scanPortParamList.add(dto);

                                // 更新域名扫描端口
                                if (!CollectionUtils.isEmpty(exitIpList)) {
                                    for (ScanHostEntity host : exitIpList) {
                                        if (!PortUtils.portEquals(host.getScanPorts(), scanDto.getScanPorts())) {
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
                                        .domain(sub.getSubDomain())
                                        .ip(ip).scanPorts(scanPorts)
                                        .company(company)
                                        .type(Const.INTEGER_3)
                                        .isMajor(RexpUtil.isMajorDomain(sub.getSubDomain()) ? Const.INTEGER_1 : Const.INTEGER_0)
                                        .subIpList(sub.getSubIpList())
                                        .build();
                                saveHostList.add(host);
                            }
                        }
                    }

                    // 保存项目-host关联关系
                    if (!exitPhList.contains(sub.getSubDomain())) {
                        ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                                .projectId(scanDto.getProjectId()).host(sub.getSubDomain())
                                .build();
                        projectHostList.add(item);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(saveHostList)) {
                scanHostService.saveBatch(saveHostList);
            }
            if (!CollectionUtils.isEmpty(updateHostList)) {
                // todo
                scanHostService.updateScanPorts(updateHostList);
//                for (ScanHostEntity host : updateHostList) {
//                    scanHostService.updateById(host);
//                }
            }
            if (!CollectionUtils.isEmpty(projectHostList)) {
                scanProjectHostService.saveBatch(projectHostList);
            }
            // 扫描端口
            if (Const.INTEGER_1.equals(scanDto.getPortFlag()) && !CollectionUtils.isEmpty(scanPortParamList)) {
                scanPortParamList = scanPortParamList.stream().distinct().collect(Collectors.toList());
//                scanPortInfoService.scanPortList(scanPortParamList);
                syncService.dataHandler(scanPortParamList);
            }
            /*List<ScanProjectContentEntity> contentList = new ArrayList<>();
            if (!StringUtils.isEmpty(scanDto.getHost())) {
                contentList = scanProjectContentService.list(new HashMap<String, Object>() {{
                    put("inputHost", scanDto.getHost());
                }});
            }
            if (!CollectionUtils.isEmpty(contentList)) {
                for (ScanProjectContentEntity content : contentList) {
                    // todo
                    content.setIsCompleted(Const.INTEGER_1);
                    scanProjectContentService.updateById(content);
                }
            }*/
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
