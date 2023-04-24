package com.lhh.servermonitor.service;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScanPortInfoService {

    @Autowired
    ScanPortService scanPortService;
    @Autowired
    ScanHostService scanHostService;

    /**
     * java代码获取开放端口
     */
    @Async
    public void scanPortList(List<ScanParamDto> dtoList) {
//        List<String> ipList = dtoList.stream().map(ScanParamDto::getSubIp).collect(Collectors.toList());
//        List<ScanPortEntity> AllHostList = CollectionUtils.isEmpty(ipList) ? new ArrayList<>() : scanPortService.getByIpList(ipList);
//        Map<String, List<ScanPortEntity>> hostMap = AllHostList.stream().collect(Collectors.groupingBy(ScanPortEntity::getIp));
        if (!CollectionUtils.isEmpty(dtoList)) {
            Map<String, Object> params = new HashMap<>();
            for (ScanParamDto dto : dtoList) {
                String ip = dto.getSubIp();
                params.put("ip", ip);
                List<ScanPortEntity> portEntityList = scanPortService.list(params);
                List<Integer> exitPorts = CollectionUtils.isEmpty(portEntityList) ? new ArrayList<>() : portEntityList.stream().map(ScanPortEntity::getPort).collect(Collectors.toList());

                log.info("开始扫描" + ip + "端口");
                String cmd = String.format(Const.STR_MASSCAN_PORT, ip, dto.getScanPorts(), 5000);
                SshResponse response = null;
                try {
                    response = ExecUtil.runCommand(cmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> portStrList = Arrays.asList(response.getOut().split("\n"));
                List<String> scanPortList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(portStrList)) {
                    for (String port : portStrList) {
                        if (!StringUtils.isEmpty(port)) {
                            port = port.substring(port.indexOf("port ") + 5, port.indexOf(Const.STR_SLASH));
                            if (!exitPorts.contains(Integer.valueOf(port))) {
                                scanPortList.add(port);
                            }
                        }
                    }
                }

                if (scanPortList.size() >= 1000) {
                    log.info(ip + "扫描端口超过1000，已忽略！");
                } else {
                    if (!CollectionUtils.isEmpty(scanPortList)) {
                        List<ScanPortEntity> portList = new ArrayList<>();
                        String ports = String.join(Const.STR_COMMA, scanPortList);
                        String nmapCmd = String.format(Const.STR_NMAP_SERVER, ports, ip);
                        SshResponse nmapResponse = null;
                        try {
                            nmapResponse = ExecUtil.runCommand(nmapCmd);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        List<String> responseLineList = Arrays.asList(nmapResponse.getOut().split("\n"));
                        List<String> serverLineList = responseLineList.stream().filter(r -> !StringUtils.isEmpty(r) && r.contains(Const.STR_SLASH) && scanPortList.contains(r.substring(0, r.indexOf(Const.STR_SLASH)))).collect(Collectors.toList());
                        Map<String, String> serverMap = new HashMap<>();
                        if (!CollectionUtils.isEmpty(serverLineList)) {
                            for (String server : serverLineList) {
                                serverMap.put(server.substring(0, server.indexOf(Const.STR_SLASH)), server.substring(server.lastIndexOf(Const.STR_BLANK)));
                            }
                        }

                        for (String port : scanPortList) {
                            ScanPortEntity scanPort = ScanPortEntity.builder()
                                    .ip(ip).port(Integer.valueOf(port))
                                    .serverName(StringUtils.isEmpty(serverMap.get(port)) ? Const.STR_CROSSBAR : serverMap.get(port))
                                    .build();
                            portList.add(scanPort);
                        }

                        // todo 保存端口可以延后步骤
                        scanPortService.saveBatch(portList);
                    }
                    log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));
                }
                JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_IP, ip));
            }
        }
    }

    /**
     * java代码获取开放端口
     */
    @Transactional(rollbackFor = Exception.class)
    public void scanSingleIpPortList(ScanParamDto dto) {
        Map<String, Object> params = new HashMap<>();
        String ip = dto.getSubIp();
        params.put("ip", ip);
        List<ScanPortEntity> portEntityList = scanPortService.list(params);
        if (!CollectionUtils.isEmpty(portEntityList)) {
            log.info(ip + "扫描端口已被扫描！");
            return;
        }
        log.info("开始扫描" + ip + "端口");
        String cmd = String.format(Const.STR_MASSCAN_PORT, ip, dto.getScanPorts(), 5000);
        SshResponse response = null;
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> portStrList = Arrays.asList(response.getOut().split("\n"));
        List<String> scanPortList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(portStrList)) {
            for (String port : portStrList) {
                if (!StringUtils.isEmpty(port)) {
                    port = port.substring(port.indexOf("port ") + 5, port.indexOf(Const.STR_SLASH));
                    scanPortList.add(port);
                }
            }
        }

        if (scanPortList.size() >= 1000) {
            log.info(ip + "扫描端口超过1000，已忽略！");
        } else {
            if (!CollectionUtils.isEmpty(scanPortList)) {
                List<ScanPortEntity> portList = new ArrayList<>();
                String ports = String.join(Const.STR_COMMA, scanPortList);
                String nmapCmd = String.format(Const.STR_NMAP_SERVER, ports, ip);
                SshResponse nmapResponse = null;
                try {
                    nmapResponse = ExecUtil.runCommand(nmapCmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> responseLineList = Arrays.asList(nmapResponse.getOut().split("\n"));
                List<String> serverLineList = responseLineList.stream().filter(r -> !StringUtils.isEmpty(r) && r.contains(Const.STR_SLASH) && scanPortList.contains(r.substring(0, r.indexOf(Const.STR_SLASH)))).collect(Collectors.toList());
                Map<String, String> serverMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(serverLineList)) {
                    for (String server : serverLineList) {
                        serverMap.put(server.substring(0, server.indexOf(Const.STR_SLASH)), server.substring(server.lastIndexOf(Const.STR_BLANK)));
                    }
                }

                for (String port : scanPortList) {
                    ScanPortEntity scanPort = ScanPortEntity.builder()
                            .ip(ip).port(Integer.valueOf(port))
                            .serverName(StringUtils.isEmpty(serverMap.get(port)) ? Const.STR_CROSSBAR : serverMap.get(port))
                            .build();
                    portList.add(scanPort);
                }

                // todo 保存端口可以延后步骤
                scanPortService.saveBatch(portList);
            }
            log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出新端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));
        }
        JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_IP, ip));
    }

    /**
     * java代码获取开放端口
     */
    public void reloadIpPortList(ScanParamDto dto) {
        Map<String, Object> params = new HashMap<>();
        String ip = dto.getSubIp();
        params.put("ip", ip);
        List<ScanPortEntity> portEntityList = scanPortService.list(params);
        List<Integer> exitPorts = CollectionUtils.isEmpty(portEntityList) ? new ArrayList<>() : portEntityList.stream().map(ScanPortEntity::getPort).collect(Collectors.toList());

        log.info("开始扫描" + ip + "端口");
        String cmd = String.format(Const.STR_MASSCAN_PORT, ip, dto.getScanPorts(), 5000);
        SshResponse response = null;
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> portStrList = Arrays.asList(response.getOut().split("\n"));
        List<String> scanPortList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(portStrList)) {
            for (String port : portStrList) {
                if (!StringUtils.isEmpty(port)) {
                    port = port.substring(port.indexOf("port ") + 5, port.indexOf(Const.STR_SLASH));
                    if (!exitPorts.contains(Integer.valueOf(port))) {
                        scanPortList.add(port);
                    }
                }
            }
        }

        if (scanPortList.size() >= 1000) {
            log.info(ip + "扫描端口超过1000，已忽略！");
        } else {
            if (!CollectionUtils.isEmpty(scanPortList)) {
                List<ScanPortEntity> portList = new ArrayList<>();
                String ports = String.join(Const.STR_COMMA, scanPortList);
                String nmapCmd = String.format(Const.STR_NMAP_SERVER, ports, ip);
                SshResponse nmapResponse = null;
                try {
                    nmapResponse = ExecUtil.runCommand(nmapCmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> responseLineList = Arrays.asList(nmapResponse.getOut().split("\n"));
                List<String> serverLineList = responseLineList.stream().filter(r -> !StringUtils.isEmpty(r) && r.contains(Const.STR_SLASH) && scanPortList.contains(r.substring(0, r.indexOf(Const.STR_SLASH)))).collect(Collectors.toList());
                Map<String, String> serverMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(serverLineList)) {
                    for (String server : serverLineList) {
                        serverMap.put(server.substring(0, server.indexOf(Const.STR_SLASH)), server.substring(server.lastIndexOf(Const.STR_BLANK)));
                    }
                }

                for (String port : scanPortList) {
                    ScanPortEntity scanPort = ScanPortEntity.builder()
                            .ip(ip).port(Integer.valueOf(port))
                            .serverName(StringUtils.isEmpty(serverMap.get(port)) ? Const.STR_CROSSBAR : serverMap.get(port))
                            .build();
                    portList.add(scanPort);
                }

                // todo 保存端口可以延后步骤
                scanPortService.saveBatch(portList);
            }
            log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));
        }
        JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_IP, ip));
    }

}
