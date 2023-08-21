package com.lhh.servermonitor.service;

import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScanPortInfoService {

    @Autowired
    RedissonClient redisson;
    @Autowired
    ScanPortService scanPortService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanProjectHostService scanProjectHostService;

    /**
     * java代码获取开放端口
     */
    public void scanIpsPortList(ScanParamDto dto) throws Exception {
        String ip = dto.getSubIp();
        String lockKey = String.format(CacheConst.REDIS_LOCK_SCANNING_IP, ip);
        RLock lock = redisson.getLock(lockKey);
        try {
            lock.lock();
            log.info("开始扫描" + ip + "端口");
            List<ScanHostEntity> hostList = scanHostService.list(new HashMap<String, Object>() {{
                put("parentDomain", ip);
            }});
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
            List<String> portStrList = StringUtils.isEmpty(response.getOut()) ? new ArrayList<>() : Arrays.asList(response.getOut().split("\n"));
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
            String company = JedisUtils.getStr(String.format(CacheConst.REDIS_DOMAIN_COMPANY, ip));
            List<ScanHostEntity> scanIpList = new ArrayList<>();
            List<String> delKeys = new ArrayList<>();
            if (!CollectionUtils.isEmpty(ipLongList)) {
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
                                .company(company)
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
                                .company(company)
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
                        scanPortService.saveBatch(savePortList);
                        log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出新端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));
                        delKeys.add(String.format(CacheConst.REDIS_SCANNING_IP, IpLongUtils.longToIp(ipLong)));
                    }
                }
            } else {
                ScanHostEntity scanIp = ScanHostEntity.builder()
                        .domain(ip).parentDomain(ip)
                        .ipLong(IpLongUtils.ipToLong(ip))
                        .scanPorts(dto.getScanPorts())
                        .company(company)
                        .type(Const.INTEGER_2).isMajor(Const.INTEGER_0)
                        .isDomain(Const.INTEGER_0)
                        .isScanning(Const.INTEGER_0)
                        .build();
                scanIpList.add(scanIp);
            }
            if (!CollectionUtils.isEmpty(scanIpList)) {
                scanHostService.saveBatch(scanIpList);
            }
            if (!CollectionUtils.isEmpty(delKeys)) {
                JedisUtils.pipeDel(delKeys);
            }
            /*try {
                scanProjectHostService.updateEndScanDomain(dto.getSubIp());
            } catch (Exception e) {
                throw new Exception();
            }*/
        } catch (Exception e) {
        } finally {
            // 判断当前线程是否持有锁
            if (lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
    }

    /**
     * java代码获取开放端口
     */
    public void scanSingleIpPortList(ScanParamDto dto) {
        String ip = dto.getSubIp();
        String domain = dto.getSubDomain();
        String lockKey = String.format(CacheConst.REDIS_LOCK_SCANNING_IP, ip);
        RLock lock = redisson.getLock(lockKey);
        try {
            lock.lock();
            Map<String, Object> params = new HashMap<>();
            Long ipLong = IpLongUtils.ipToLong(ip);
            if (Const.STR_CROSSBAR.equals(ip)) {
                // 更新isScanning
                log.info("域名" + domain + ":" + ip + "扫描端口已被扫描(一)！");
//                scanHostService.updateEndScanIp(domain, ip, ipLong, dto.getScanPorts());
                JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_IP, ip));
                return;
            }
            params.put("ipLong", ipLong);
            List<ScanHostEntity> ipList = scanHostService.basicList(params);
            List<ScanPortEntity> exitPortEntityList = scanPortService.basicList(params);
            // 第二个判断是为了防止host表先存了数据导致不扫描port
            String portParam = PortUtils.getNewPorts(ipList.get(0).getScanPorts(), dto.getScanPorts());
            if (!CollectionUtils.isEmpty(ipList) && !CollectionUtils.isEmpty(exitPortEntityList)) {
                if (PortUtils.portEquals(ipList.get(0).getScanPorts(), dto.getScanPorts())) {
                    // 更新isScanning
                    log.info("域名" + domain + ":" + ip + "扫描端口已被扫描(一)！");
//                    scanHostService.updateEndScanIp(domain, ip, ipLong, ipList.get(0).getScanPorts());
                    JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_IP, ip));
                    return;
                } else {
                    portParam = PortUtils.getNewPorts(ipList.get(0).getScanPorts(), dto.getScanPorts());
                }
            } else {
                portParam = dto.getScanPorts();
            }
            log.info("开始扫描" + domain + ":" + ip + "端口");
            String cmd = String.format(Const.STR_MASSCAN_PORT, ip, portParam);
            SshResponse response = null;
            try {
                response = ExecUtil.runCommand(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<String> scanPortList = new ArrayList<>();
            if (!StringUtils.isEmpty(response.getOut())) {
                List<String> portStrList = Arrays.asList(response.getOut().split("\n"));
                if (!CollectionUtils.isEmpty(portStrList)) {
                    for (String port : portStrList) {
                        if (!StringUtils.isEmpty(port)) {
                            port = port.substring(port.indexOf("port ") + 5, port.indexOf(Const.STR_SLASH)).trim();
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
                            serverMap.put(server.substring(0, server.indexOf(Const.STR_SLASH)), server.substring(server.lastIndexOf(Const.STR_BLANK)).trim());
                        }
                    }

                    exitPortEntityList = scanPortService.basicList(params);
                    List<Integer> exitPortList = exitPortEntityList.stream().map(ScanPortEntity::getPort).collect(Collectors.toList());
                    for (String p : scanPortList) {
                        Integer port = Integer.valueOf(p);
                        if (!exitPortList.contains(port)) {
                            ScanPortEntity scanPort = ScanPortEntity.builder()
                                    .ip(ip).ipLong(ipLong).port(port)
                                    .serverName(StringUtils.isEmpty(port) ? Const.STR_CROSSBAR : serverMap.get(p))
                                    .build();
                            portList.add(scanPort);
                        }
                    }

                    // todo 保存端口可以延后步骤
                    if (!CollectionUtils.isEmpty(portList)) {
                        scanPortService.saveBatch(portList);
                    }
                }
                log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出新端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));
            }
            // 更新isScanning
//            scanHostService.updateEndScanIp(domain, ip, ipLong, portParam);
            JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_IP, ip));
        } catch (Exception e) {
        } finally {
            // 判断当前线程是否持有锁
            if (lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
    }

}
