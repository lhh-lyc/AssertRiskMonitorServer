package com.lhh.serverTask.service;

import com.lhh.serverTask.dao.ScanHostDao;
import com.lhh.serverTask.dao.ScanProjectHostDao;
import com.lhh.serverTask.mqtt.MqHostSender;
import com.lhh.serverTask.utils.ExecUtil;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ReScanDto;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.entity.SshResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParentDomainService {

    @Value("${dir-setting.subfinder-dir}")
    private String subfinderDir;

    @Autowired
    private ScanProjectHostDao scanProjectHostDao;
    @Autowired
    private ScanProjectHostService scanProjectHostService;
    @Autowired
    private ScanHostDao scanHostDao;
    @Autowired
    private MqHostSender mqHostSender;

    public void scanDomain(ReScanDto scanDto) {
        if (!CollectionUtils.isEmpty(scanDto.getHostList())) {
            for (String host : scanDto.getHostList()) {
                List<String> subdomainList = new ArrayList<>();
                log.info(host + "子域名收集");
                // 子域名列表
                String cmd = String.format(Const.STR_SUBFINDER_SUBDOMAIN, subfinderDir, host);
                SshResponse response = null;
                try {
                    response = ExecUtil.runCommand(cmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                subdomainList = response.getOutList();
                subdomainList = subdomainList.stream().distinct().collect(Collectors.toList());
                log.info(CollectionUtils.isEmpty(subdomainList) ? host+ "未扫描到子域名" : host + "子域名有:" + String.join(Const.STR_COMMA, subdomainList));
                if (!CollectionUtils.isEmpty(subdomainList)) {
                    log.info("重新扫描:"+String.join(Const.STR_COMMA, subdomainList));
                    mqHostSender.sendReScanHostToMqtt(host, subdomainList);
                }
                // 保存新增的项目-子域名关联关系
                List<ScanProjectHostEntity> proHostEntityList = scanProjectHostDao.queryByHostList(subdomainList);
                List<String> proHostList = proHostEntityList.stream().map(ScanProjectHostEntity::getHost).collect(Collectors.toList());
                subdomainList.removeAll(proHostList);
                if (!CollectionUtils.isEmpty(subdomainList)) {
                    List<ScanProjectHostEntity> saveList = new ArrayList<>();
                    ScanProjectHostEntity parent = scanProjectHostDao.queryByHost(host);
                    for (String s : subdomainList) {
                        ScanProjectHostEntity sub = ScanProjectHostEntity.builder()
                                .parentDomain(host).host(s).projectId(parent.getProjectId()).isScanning(Const.INTEGER_0)
                                .build();
                        saveList.add(sub);
                    }
                    scanProjectHostService.saveBatch(saveList);
                }
            }
        }
    }

}
