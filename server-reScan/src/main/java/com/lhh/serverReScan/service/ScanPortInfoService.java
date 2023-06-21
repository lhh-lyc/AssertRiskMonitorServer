package com.lhh.serverReScan.service;

import com.lhh.serverReScan.dao.ScanPortDao;
import com.lhh.serverReScan.utils.ExecUtil;
import com.lhh.serverReScan.utils.RedisUtils;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.IpLongUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    ScanPortService scanPortService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    RedisUtils redisUtils;

    /**
     * java代码获取开放端口
     */
    public Boolean scanSingleIpPortList(String domain, String ip) {
        Long ipLong = IpLongUtils.ipToLong(ip);
        if (redisUtils.isMember("re_ip", ip)) {
            List<ScanPortEntity> portEntityList = scanPortDao.queryList(ipLong);
            if (CollectionUtils.isEmpty(portEntityList)) {
                return false;
            } else {
                return true;
            }
        }
        Map<String, Object> params = new HashMap<>();
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

                for (String port : scanPortList) {
                    ScanPortEntity scanPort = ScanPortEntity.builder()
                            .ip(ip).ipLong(ipLong).port(Integer.valueOf(port))
                            .serverName(StringUtils.isEmpty(serverMap.get(port)) ? Const.STR_CROSSBAR : serverMap.get(port))
                            .build();
                    portList.add(scanPort);
                }
                List<ScanPortEntity> portEntityList = scanPortDao.queryList(ipLong);
                List<Long> deleteIds = portEntityList.stream().map(ScanPortEntity::getPortId).collect(Collectors.toList());
                scanPortService.removeByIds(deleteIds);
                // todo 保存端口可以延后步骤
                scanPortService.saveBatch(portList);
            }
            log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出新端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));
        }
        redisUtils.addSet("lock_re_ip", "re_ip", ip);
        return true;
    }

}
