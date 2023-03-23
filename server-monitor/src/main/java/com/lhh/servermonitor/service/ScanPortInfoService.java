package com.lhh.servermonitor.service;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.CacheConst;
import com.lhh.serverbase.utils.Const;
import com.lhh.servermonitor.dto.ScanParamDto;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.JedisUtils;
import com.lhh.servermonitor.utils.PortUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
    ScanPortService scanPortService;
    @Autowired
    ScanHostService scanHostService;

    /**
     * java代码获取开放端口
     */
    @Async
    public void scanPortList(List<ScanParamDto> dtoList) {
        if (!CollectionUtils.isEmpty(dtoList)) {
            for (ScanParamDto dto : dtoList) {
                String ip = dto.getSubIp();
                List<ScanPortEntity> hostList = scanPortService.getByIpList(Arrays.asList(ip));
                // 已扫描的不再扫描
                if (!CollectionUtils.isEmpty(hostList)) {
                    JedisUtils.delKey(String.format(CacheConst.REDIS_TASK_IP, ip));
                    log.info("" + ip + "端口已扫描");
                    continue;
                }
                List<Integer> ports = hostList.stream().map(ScanPortEntity::getPort).collect(Collectors.toList());

                log.info("开始扫描" + ip + "端口");
                List<ScanPortEntity> portList = new ArrayList<>();
                String cmd = String.format(Const.STR_MASSCAN_PORT, ip, dto.getScanPorts(), 5000);
                SshResponse response = null;
                try {
                    response = ExecUtil.runCommand(cmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> portStrList = Arrays.asList(response.getOut().split("\n"));
                List<Integer> scanPortList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(portStrList)) {
                    for (String port : portStrList) {
                        if (!StringUtils.isEmpty(port)) {
                            port = port.substring(port.indexOf("port ") + 5, port.indexOf(Const.STR_SLASH));
                            if (!ports.contains(Integer.valueOf(port))) {
                                ScanPortEntity scanPort = ScanPortEntity.builder()
                                        .ip(ip).port(Integer.valueOf(port))
                                        .build();
                                portList.add(scanPort);
                                scanPortList.add(Integer.valueOf(port));
                            }
                        }
                    }
                    // todo 保存端口可以延后步骤
                    scanPortService.saveBatch(portList);
                }
                JedisUtils.delKey(String.format(CacheConst.REDIS_TASK_IP, ip));
                log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出端口" : ip + "扫描出端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i->String.valueOf(i)).collect(Collectors.toList())));
            }
        }
    }

}
