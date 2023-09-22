package com.lhh.servermonitor.service;

import com.alibaba.fastjson2.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.*;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.servermonitor.dao.HostCompanyDao;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScanPortInfoService {

    @Autowired
    RedissonClient redisson;
    @Autowired
    HostCompanyDao hostCompanyDao;
    @Autowired
    ScanPortService scanPortService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    HostCompanyService hostCompanyService;
    @Autowired
    TmpRedisService tmpRedisService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * java代码获取开放端口
     */
    public void scanIpsPortList(ScanParamDto dto) throws Exception {
        String ip = dto.getSubIp();
        Long ipLong = IpLongUtils.ipToLong(ip);
        String lockKey = String.format(CacheConst.REDIS_LOCK_SCANNING_IP, ip);
        RLock lock = redisson.getLock(lockKey);
        try {
            lock.lock();
            log.info("开始扫描" + ip + "端口");
            String ports = tmpRedisService.getDomainScanPorts(dto.getSubIp());
            if (PortUtils.portEquals(ports, dto.getScanPorts())) {
                // 扫描ip端口，推迟缓存有效期，目的是让后面相同的ip不用重复扫描
                stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_IP, ipLong), PortUtils.getAllPorts(ports, dto.getScanPorts()), 60 * 60 * 24L, TimeUnit.SECONDS);
                log.info("ip:" + ip + "扫描端口已被扫描(一)！");
                return;
            }
            if (Const.INTEGER_1.equals(dto.getPortTool())) {
                masscanPort(dto);
            } else {
                nmapPort(dto);
            }
            stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_IP, ipLong), PortUtils.getAllPorts(ports, dto.getScanPorts()), 60 * 60 * 24L, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.info("扫描端口异常", e);
            throw new Exception(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * java代码获取开放端口
     */
    public void scanSingleIpPortList(ScanParamDto dto) throws Exception {
        String ip = dto.getSubIp();
        Long ipLong = IpLongUtils.ipToLong(ip);
        String domain = dto.getSubDomain();
        String lockKey = String.format(CacheConst.REDIS_LOCK_SCANNING_IP, ip);
        RLock lock = redisson.getLock(lockKey);
        try {
            // 必须用lock阻塞不能用tryLock
            // tryLock会导致一个子域名跳过此步骤，端口未即时扫出来，下一步域名+端口的url扫描缺失
            lock.lock();
            if (Const.STR_CROSSBAR.equals(ip)) {
                log.info("域名" + domain + "ip为空！");
                return;
            }
            // 扫描ip端口，推迟缓存有效期，目的是让后面相同的ip不用重复扫描
            String ports = tmpRedisService.getDomainScanPorts(dto.getHost());
            if (PortUtils.portEquals(ports, dto.getScanPorts())) {
                stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_IP, ipLong), PortUtils.getAllPorts(ports, dto.getScanPorts()), 60 * 60 * 24L, TimeUnit.SECONDS);
                log.info("ip:" + ip + "已被扫描!");
                return;
            }
            log.info("开始扫描" + domain + ":" + ip + "端口");
            if (Const.INTEGER_1.equals(dto.getPortTool())) {
                masscanPort(dto);
            } else {
                nmapPort(dto);
            }
            stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_IP, ipLong), PortUtils.getAllPorts(ports, dto.getScanPorts()), 60 * 60 * 24L, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.info("扫描端口异常", e);
            throw new Exception(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void masscanPort(ScanParamDto dto) {
        String ip = dto.getSubIp();
        Long ipLong = IpLongUtils.ipToLong(ip);
        String cmd = String.format(Const.STR_MASSCAN_PORT, ip, dto.getScanPorts());
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

        if (scanPortList.size() > Const.INTEGER_1000) {
            log.info(ip + "扫描端口超过1000，已忽略！");
        } else {
            List<ScanPortEntity> portEntityList = scanPortService.basicByIpList(Arrays.asList(ipLong));
            List<Integer> queryPortList = portEntityList.stream().map(ScanPortEntity::getPort).collect(Collectors.toList());
            List<String> exitPortList = queryPortList.stream().map(Object::toString).collect(Collectors.toList());
            scanPortList.removeAll(exitPortList);
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
                List<String> serverLineList = responseLineList.stream().filter(r -> !StringUtils.isEmpty(r)).collect(Collectors.toList());
                Map<String, String> serverMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(serverLineList)) {
                    for (String server : serverLineList) {
                        String status = server.substring(server.indexOf(Const.STR_BLANK), server.lastIndexOf(Const.STR_BLANK)).trim();
                        if ("open".equals(status)) {
                            serverMap.put(server.substring(0, server.indexOf(Const.STR_SLASH)), server.substring(server.lastIndexOf(Const.STR_BLANK)).trim());
                        }
                    }
                }
                for (String p : scanPortList) {
                    Integer port = Integer.valueOf(p);
                    ScanPortEntity scanPort = ScanPortEntity.builder()
                            .ip(ip).ipLong(ipLong).port(port)
                            .serverName(StringUtils.isEmpty(port) ? Const.STR_CROSSBAR : serverMap.get(p))
                            .build();
                    portList.add(scanPort);
                }

                if (!CollectionUtils.isEmpty(portList)) {
                    scanPortService.saveBatch(portList);
                }
            }
            log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出新端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));
        }
    }

    public void nmapPort(ScanParamDto dto) {
        String ip = dto.getSubIp();
        Long ipLong = IpLongUtils.ipToLong(ip);
        String cmd = String.format(Const.STR_NMAP_SERVER, dto.getScanPorts(), ip);
        SshResponse response = null;
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> responseLineList = Arrays.asList(response.getOut().split("\n"));
        List<String> serverLineList = responseLineList.stream().filter(r -> !StringUtils.isEmpty(r)).collect(Collectors.toList());
        Map<String, String> serverMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(serverLineList)) {
            for (String server : serverLineList) {
                String status = server.substring(server.indexOf(Const.STR_BLANK), server.lastIndexOf(Const.STR_BLANK)).trim();
                if ("open".equals(status)) {
                    serverMap.put(server.substring(0, server.indexOf(Const.STR_SLASH)), server.substring(server.lastIndexOf(Const.STR_BLANK)).trim());
                }
            }
        }

        if (serverMap.size() > Const.INTEGER_1000) {
            log.info(ip + "扫描端口超过1000，已忽略！");
        } else {
            List<ScanPortEntity> saveportList = new ArrayList<>();
            List<Integer> portList = new ArrayList<>();
            for (String p : serverMap.keySet()) {
                Integer port = Integer.valueOf(p);
                portList.add(port);
                ScanPortEntity scanPort = ScanPortEntity.builder()
                        .ip(ip).ipLong(ipLong).port(port)
                        .serverName(StringUtils.isEmpty(port) ? Const.STR_CROSSBAR : serverMap.get(p))
                        .build();
                saveportList.add(scanPort);
            }

            if (!CollectionUtils.isEmpty(saveportList)) {
                scanPortService.saveBatch(saveportList);
            }
            log.info(CollectionUtils.isEmpty(portList) ? ip + "未扫描出新端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, portList.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));
        }
    }

}
