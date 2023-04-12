package com.lhh.servermonitor.service;

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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        List<String> ipList = dtoList.stream().map(ScanParamDto::getSubIp).collect(Collectors.toList());
        List<ScanPortEntity> AllHostList = CollectionUtils.isEmpty(ipList) ? new ArrayList<>() : scanPortService.getByIpList(ipList);
        Map<String, List<ScanPortEntity>> hostMap = AllHostList.stream().collect(Collectors.groupingBy(ScanPortEntity::getIp));
        if (!CollectionUtils.isEmpty(dtoList)) {
            for (ScanParamDto dto : dtoList) {
                String ip = dto.getSubIp();
                List<ScanPortEntity> hostList = hostMap.get(ip);
                List<Integer> exitPorts = CollectionUtils.isEmpty(hostList) ? new ArrayList<>() : hostList.stream().map(ScanPortEntity::getPort).collect(Collectors.toList());

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
                            if (!exitPorts.contains(Integer.valueOf(port))) {
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
                JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_IP, ip));
                log.info(CollectionUtils.isEmpty(scanPortList) ? ip + "未扫描出端口" : ip + "扫描出新端口:" + String.join(Const.STR_COMMA, scanPortList.stream().map(i->String.valueOf(i)).collect(Collectors.toList())));
            }
        }
    }

}
