package com.lhh.servermonitor.service;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.servermonitor.mqtt.MqIpSender;
import com.lhh.servermonitor.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Service
public class InitService {

    @Value(value = "${sync-setting.MAX_THREADS}")
    private Integer MAX_THREADS;
    @Value(value = "${sync-setting.EXPIRED_PAGE_SIZE}")
    private Integer EXPIRED_PAGE_SIZE;
    @Value(value = "${monitor-setting.is-init}")
    private Integer isInit;

    @Autowired
    ScanProjectService scanProjectService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    MqIpSender mqIpSender;

    public void initTask() {
        // 扫描中断的ip发送到mq，只允许一个服务发送
        if (Const.INTEGER_1.equals(isInit)) {
            Integer size = MAX_THREADS * EXPIRED_PAGE_SIZE;
            List<ScanParamDto> dtoList = new ArrayList<>();
            Map<String, String> keyList = new HashMap<>();
            Set<String> ipKeySet = JedisUtils.keysS(String.format(CacheConst.REDIS_SCANNING_IP, '*'));
            if (!CollectionUtils.isEmpty(ipKeySet)) {
                Map<String, String> map = JedisUtils.getPipeJson(new ArrayList<>(ipKeySet));
                for (String key : map.keySet()) {
                    JSONObject obj = JSONObject.parseObject(map.get(key));
                    String ipScanPorts = obj == null || obj.get("ports") == null ? Const.STR_EMPTY : MapUtil.getStr(obj, "ports");
                    String status = MapUtil.getStr(obj, "status");
                    if (Const.STR_1.equals(status)) {
                        continue;
                    }
                    ScanParamDto dto = ScanParamDto.builder()
                            .subIp(key.split(Const.STR_COLON)[1])
                            .scanPorts(ipScanPorts)
                            .build();
                    dtoList.add(dto);
                    obj.put("status", Const.STR_1);
                    keyList.put(key, JSONObject.toJSONString(obj));
                    if (dtoList.size() == size) {
                        ScanParamDto d = ScanParamDto.builder().dtoList(dtoList).build();
                        mqIpSender.sendScanningIpToMqtt(d);
                        dtoList.clear();
                        JedisUtils.setPipeJson(keyList);
                        keyList.clear();
                    }
                }
                if (!CollectionUtils.isEmpty(dtoList)) {
                    ScanParamDto d = ScanParamDto.builder().dtoList(dtoList).build();
                    mqIpSender.sendScanningIpToMqtt(d);
                    JedisUtils.setPipeJson(keyList);
                }
            }
        }
    }

}
