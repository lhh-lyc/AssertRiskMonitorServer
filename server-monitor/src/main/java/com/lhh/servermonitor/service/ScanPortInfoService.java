package com.lhh.servermonitor.service;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Integers;
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
    @Autowired
    ScanProjectHostService scanProjectHostService;

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
                String cmd = String.format(Const.STR_MASSCAN_PORT, ip, dto.getScanPorts());
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
                            port = port.substring(port.indexOf("port ") + 5, port.indexOf(Const.STR_SLASH)).trim();
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
    public void scanIpsPortList(ScanParamDto dto) {
        String ip = dto.getSubIp();
        log.info("开始扫描" + ip + "端口");
        List<ScanHostEntity> hostList = scanHostService.list(new HashMap<String, Object>(){{put("parentDomain", ip);}});
        if (!CollectionUtils.isEmpty(hostList) && PortUtils.portEquals(hostList.get(0).getScanPorts(), dto.getScanPorts())) {
            // 更新isScanning
            log.info("ip:" + ip + "扫描端口已被扫描(一)！");
            JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_IP, ip));
            return;
        }
        String cmd = String.format(Const.STR_MASSCAN_PORT, ip, dto.getScanPorts());
        SshResponse response = null;
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> portStrList = Arrays.asList(response.getOut().split("\n"));
        Map<Long, List<String>> ipMap = new HashMap<>();
        List<Long> ipLongList = new ArrayList<>();
        List<Long> loseIpList = new ArrayList<>();
        List<String> portList;
        if (!CollectionUtils.isEmpty(portStrList)) {
            for (String port : portStrList) {
                if (!StringUtils.isEmpty(port)) {
                    String scanIp = port.substring(port.indexOf("on ") + 3).trim();
                    Long ipLong = IpLongUtils.ipToLong(scanIp);
                    if (loseIpList.contains(ipLong)) {
                        continue;
                    }
                    if (ipMap.containsKey(ipLong)) {
                        portList = ipMap.get(ipLong);
                    } else {
                        ipLongList.add(ipLong);
                        portList = new ArrayList<>();
                    }
                    port = port.substring(port.indexOf("port ") + 5, port.indexOf(Const.STR_SLASH)).trim();
                    portList.add(port);
                    if (portList.size() > 1000) {
                        log.info(ip + "扫描端口超过1000，已忽略！");
                        ipMap.remove(scanIp);
                        loseIpList.add(ipLong);
                    } else {
                        ipMap.put(ipLong, portList);
                    }
                }
            }
        }
        List<ScanHostEntity> saveIpList = new ArrayList<>();
        List<ScanHostEntity> updateIpList = new ArrayList<>();
        List<ScanHostEntity> exitIpList = scanHostService.getIpByIpList(ipLongList);
        exitIpList = exitIpList.stream().distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(exitIpList)) {
            for (ScanHostEntity exitIp : exitIpList) {
                if (!PortUtils.portEquals(exitIp.getScanPorts(), dto.getScanPorts())) {
                    exitIp.setScanPorts(PortUtils.getNewPorts(exitIp.getScanPorts(), dto.getScanPorts()));
                    updateIpList.add(exitIp);
                    continue;
                }
                ipMap.remove(exitIp.getIpLong());
                // 已存在的ip维护关联关系
//                if (dto.getSubIp().contains(Const.STR_SLASH)) {
                    ScanHostEntity scanIp = ScanHostEntity.builder()
                            .domain(ip).parentDomain(ip)
                            .ip(IpLongUtils.longToIp(exitIp.getIpLong())).ipLong(exitIp.getIpLong())
                            .scanPorts(PortUtils.getNewPorts(exitIp.getScanPorts(), dto.getScanPorts()))
                            .company(Const.STR_CROSSBAR)
                            .type(Const.INTEGER_2).isMajor(Const.INTEGER_0)
                            .isDomain(Const.INTEGER_0)
                            .isScanning(Const.INTEGER_0)
                            .build();
                    saveIpList.add(scanIp);
//                }
            }
            if (!CollectionUtils.isEmpty(saveIpList)) {
                scanHostService.saveBatch(saveIpList);
            }
            // todo
            if (!CollectionUtils.isEmpty(updateIpList)) {
                for (ScanHostEntity host : updateIpList) {
                    scanHostService.updateById(host);
                }
            }
        }

        List<ScanPortEntity> portEntityList = scanPortService.basicByIpList(ipLongList);
        Map<Long, List<Integer>> portMap = portEntityList.stream()
                .collect(Collectors.groupingBy(ScanPortEntity::getIpLong,
                        Collectors.mapping(ScanPortEntity::getPort, Collectors.toList())));

        List<String> scanPortList = new ArrayList<>();
        List<ScanHostEntity> scanIpList = new ArrayList<>();
        List<String> delKeys = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ipMap)) {
            for (Long ipLong : ipMap.keySet()) {
                List<String> exitPortList = CollectionUtils.isEmpty(portMap.get(ipLong)) ? new ArrayList<>() :
                        portMap.get(ipLong).stream().map(Object::toString).collect(Collectors.toList());
                scanPortList = ipMap.get(ipLong);
                scanPortList.removeAll(exitPortList);
                if (CollectionUtils.isEmpty(scanPortList)) {
                    continue;
                }
                List<ScanPortEntity> savePortList = new ArrayList<>();
                String ports = String.join(Const.STR_COMMA, scanPortList);
                String nmapCmd = String.format(Const.STR_NMAP_SERVER, ports, IpLongUtils.longToIp(ipLong));
                SshResponse nmapResponse = null;
                try {
                    nmapResponse = ExecUtil.runCommand(nmapCmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> responseLineList = Arrays.asList(nmapResponse.getOut().split("\n"));
                List<String> finalScanPortList = scanPortList;
                List<String> serverLineList = responseLineList.stream().filter(r -> !StringUtils.isEmpty(r) && r.contains(Const.STR_SLASH) && finalScanPortList.contains(r.substring(0, r.indexOf(Const.STR_SLASH)))).collect(Collectors.toList());
                Map<String, String> serverMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(serverLineList)) {
                    for (String server : serverLineList) {
                        serverMap.put(server.substring(0, server.indexOf(Const.STR_SLASH)), server.substring(server.lastIndexOf(Const.STR_BLANK)));
                    }
                }

                ScanHostEntity scanIp = ScanHostEntity.builder()
                        .domain(ip).parentDomain(ip)
                        .ip(IpLongUtils.longToIp(ipLong)).ipLong(ipLong)
                        .scanPorts(dto.getScanPorts())
                        .company(Const.STR_CROSSBAR)
                        .type(Const.INTEGER_2).isMajor(Const.INTEGER_0)
                        .isDomain(Const.INTEGER_0)
                        .isScanning(Const.INTEGER_0)
                        .build();
                scanIpList.add(scanIp);

                for (String port : scanPortList) {
                    ScanPortEntity scanPort = ScanPortEntity.builder()
                            .ip(IpLongUtils.longToIp(ipLong)).ipLong(ipLong).port(Integer.valueOf(port))
                            .serverName(StringUtils.isEmpty(serverMap.get(port)) ? Const.STR_CROSSBAR : serverMap.get(port))
                            .build();
                    savePortList.add(scanPort);
                }

                // todo 保存端口可以延后步骤
                scanPortService.saveBatch(savePortList);
                log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出新端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));
                delKeys.add(String.format(CacheConst.REDIS_SCANNING_IP, IpLongUtils.longToIp(ipLong)));
            }
        }
        if (!CollectionUtils.isEmpty(scanIpList)) {
            scanHostService.saveBatch(scanIpList);
        }
        if (!CollectionUtils.isEmpty(delKeys)) {
            JedisUtils.pipeDel(delKeys);
        }
        log.info("开始更新project_host=" + dto.getSubIp() + "数据状态");
        try {
            scanProjectHostService.updateEndScanDomain(dto.getSubIp());
        } catch (Exception e) {
            log.error("更新project_host=" + dto.getSubIp() + "数据状态出现问题,异常详情：", e);
        }
        log.info("更新结束project_host=" + dto.getSubIp() + "数据状态");
    }

        /**
         * java代码获取开放端口
         */
    public void scanSingleIpPortList(ScanParamDto dto) {
        Map<String, Object> params = new HashMap<>();
        String ip = dto.getSubIp();
        String domain = dto.getSubDomain();
        Long ipLong = IpLongUtils.ipToLong(ip);
        params.put("ipLong", ipLong);
        List<ScanPortEntity> portEntityList = scanPortService.basicList(params);
        if (!CollectionUtils.isEmpty(portEntityList)) {
            // 更新isScanning
            log.info("域名" + domain + ":" + ip + "扫描端口已被扫描(一)！");
            log.info("开始更新" + domain + ":" + ip + "数据状态(ipLong=" + ipLong + ")");
            scanHostService.updateEndScanIp(ipLong, domain);
            log.info("更新结束" + domain + ":" + ip + "数据状态(ipLong=" + ipLong + ")");
            JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_IP, ip));
            return;
        }
        log.info("开始扫描" + domain + ":" + ip + "端口");
        String cmd = String.format(Const.STR_MASSCAN_PORT, ip, dto.getScanPorts());
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
                    port = port.substring(port.indexOf("port ") + 5, port.indexOf(Const.STR_SLASH)).trim();
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
                            .ip(ip).ipLong(ipLong).port(Integer.valueOf(port))
                            .serverName(StringUtils.isEmpty(serverMap.get(port)) ? Const.STR_CROSSBAR : serverMap.get(port))
                            .build();
                    portList.add(scanPort);
                }

                // todo 保存端口可以延后步骤
                scanPortService.saveBatch(portList);
            }
            log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出新端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));
        }
        // 更新isScanning
        log.info("开始更新" + domain + ":" + ip + "数据状态(ipLong=" + ipLong + ")");
        try {
            scanHostService.updateEndScanIp(ipLong, domain);
        } catch (Exception e) {
            log.error(domain + ":" + ip + "更新状态出现错误：", e);
        }
        log.info("更新结束" + domain + ":" + ip + "数据状态(ipLong=" + ipLong + ")");
        JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_IP, ip));
    }

    /**
     * 重新扫描，覆盖
     */
    public void reloadIpPortList(ScanParamDto dto) {
        Map<String, Object> params = new HashMap<>();
        String ip = dto.getSubIp();
        params.put("ip", ip);
        List<ScanPortEntity> portEntityList = scanPortService.list(params);
        List<Integer> exitPorts = CollectionUtils.isEmpty(portEntityList) ? new ArrayList<>() : portEntityList.stream().map(ScanPortEntity::getPort).collect(Collectors.toList());

        log.info("开始扫描" + ip + "端口");
        String cmd = String.format(Const.STR_MASSCAN_PORT, ip, dto.getScanPorts());
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
                    port = port.substring(port.indexOf("port ") + 5, port.indexOf(Const.STR_SLASH)).trim();
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
