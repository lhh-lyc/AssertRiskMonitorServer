package com.lhh.serverTask.service;

import com.lhh.serverTask.dao.ScanPortDao;
import com.lhh.serverTask.dao.ScanProjectHostDao;
import com.lhh.serverTask.utils.ExecUtil;
import com.lhh.serverTask.utils.RedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanAddRecordEntity;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.IpLongUtils;
import lombok.extern.slf4j.Slf4j;
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
    ScanPortDao scanPortDao;
    @Autowired
    ScanProjectHostDao scanProjectHostDao;
    @Autowired
    ScanPortService scanPortService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    ScanAddRecordService scanAddRecordService;
    @Autowired
    RedisUtils redisUtils;

    /**
     * java代码获取开放端口
     */
    public Boolean scanSingleIpPortList(String domain, String ip) {
        if (redisUtils.hasKey(String.format(CacheConst.REDIS_TASKING_IP, ip))) {
            String flag = redisUtils.getString(String.format(CacheConst.REDIS_TASKING_IP, ip));
            return Boolean.valueOf(flag);
        }
        Map<String, Object> params = new HashMap<>();
        Long ipLong = IpLongUtils.ipToLong(ip);
        params.put("ipLong", ipLong);
        log.info("开始扫描" + domain + ":" + ip + "端口");
        String cmd = String.format(Const.STR_MASSCAN_PORT, ip, "1-65535");
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
            return false;
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

                List<ScanAddRecordEntity> recordList = new ArrayList<>();
                List<Long> projectIdList = scanProjectHostDao.queryProjectIdByIp(ipLong);
                List<ScanPortEntity> exitPortList = scanPortDao.queryList(ipLong);
                List<String> exitPorts = exitPortList.stream().map(ScanPortEntity::getPort).map(Objects::toString).collect(Collectors.toList());
                // 新增部分
                List<String> addList = scanPortList.stream().filter(p -> !exitPorts.contains(p)).collect(Collectors.toList());
                // 删除部分
                List<ScanPortEntity> delList = exitPortList.stream().filter(p -> !scanPortList.contains(p.getPort().toString())).collect(Collectors.toList());

                for (String port : addList) {
                    ScanPortEntity scanPort = ScanPortEntity.builder()
                            .ip(ip).ipLong(ipLong).port(Integer.valueOf(port))
                            .serverName(StringUtils.isEmpty(serverMap.get(port)) ? Const.STR_CROSSBAR : serverMap.get(port))
                            .build();
                    portList.add(scanPort);
                    if (!CollectionUtils.isEmpty(projectIdList)) {
                        for (Long id : projectIdList) {
                            ScanAddRecordEntity record = ScanAddRecordEntity.builder()
                                    .projectId(id).parentName(String.valueOf(ipLong)).subName(port).addRecordType(Const.INTEGER_2)
                                    .build();
                            recordList.add(record);
                        }
                    }
                }
                if (!CollectionUtils.isEmpty(delList)) {
                    List<Long> deleteIds = delList.stream().map(ScanPortEntity::getPortId).collect(Collectors.toList());
                    scanPortService.deleteBatch(deleteIds);
                }
                // todo 保存端口可以延后步骤
                scanPortService.saveBatch(portList);
                if (!CollectionUtils.isEmpty(recordList)) {
                    scanAddRecordService.saveBatch(recordList);
                }
            }
            log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出新端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));
        }
        return true;
    }

}
