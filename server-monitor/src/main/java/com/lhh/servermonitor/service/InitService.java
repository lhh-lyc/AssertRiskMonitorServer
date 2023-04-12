package com.lhh.servermonitor.service;

import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.servermonitor.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class InitService {

    @Autowired
    ScanProjectService scanProjectService;
    @Autowired
    ScanPortInfoService scanPortInfoService;

    public void initTask() {
        /*List<ScanProjectEntity> projectList = new ArrayList<>();
        Set<String> projectKeySet = JedisUtils.keysS(String.format(CacheConst.REDIS_SCANNING_PROJECT, '*'));
        if (!CollectionUtils.isEmpty(projectKeySet)) {
            Map<String, String> map = JedisUtils.getPipeJson(new ArrayList<>(projectKeySet));
            if (!CollectionUtils.isEmpty(map)) {
                for (String key : map.keySet()) {
                    ScanProjectEntity project = JSONObject.toJavaObject(JSONObject.parseObject(map.get(key)), ScanProjectEntity.class);
                    projectList.add(project);
                }
            }
        }
        if (!CollectionUtils.isEmpty(projectList)) {
            for (ScanProjectEntity project : projectList) {
                scanProjectService.saveProject(project);
            }
        }*/

        List<ScanParamDto> dtoList = new ArrayList<>();
        Set<String> ipKeySet = JedisUtils.keysS(String.format(CacheConst.REDIS_SCANNING_IP, '*'));
        if (!CollectionUtils.isEmpty(ipKeySet)) {
            Map<String, String> map = JedisUtils.getPipeJson(new ArrayList<>(ipKeySet));
            for (String key : map.keySet()) {
                ScanParamDto dto = ScanParamDto.builder()
                        .subIp(key.split(Const.STR_COLON)[1])
                        .scanPorts(map.get(key))
                        .build();
                dtoList.add(dto);
                if (dtoList.size() == 500) {
                    scanPortInfoService.scanPortList(dtoList);
                    dtoList.clear();
                }
            }
            if (!CollectionUtils.isEmpty(dtoList)) {
                scanPortInfoService.scanPortList(dtoList);
            }
        }
    }

}
