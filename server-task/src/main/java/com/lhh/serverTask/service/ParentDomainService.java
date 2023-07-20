package com.lhh.serverTask.service;

import com.lhh.serverTask.dao.ScanHostDao;
import com.lhh.serverTask.dao.ScanProjectHostDao;
import com.lhh.serverTask.mqtt.MqHostSender;
import com.lhh.serverTask.utils.ExecUtil;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ReScanDto;
import com.lhh.serverbase.entity.ScanAddRecordEntity;
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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParentDomainService {

    @Value("${dir-setting.tool-dir}")
    private String toolDir;

    @Autowired
    private ScanProjectHostDao scanProjectHostDao;
    @Autowired
    private ScanProjectHostService scanProjectHostService;
    @Autowired
    private ScanAddRecordService scanAddRecordService;
    @Autowired
    private MqHostSender mqHostSender;

    public void scanDomain(ReScanDto scanDto) {
        if (!CollectionUtils.isEmpty(scanDto.getHostList())) {
            for (String host : scanDto.getHostList()) {
                List<String> subdomainList = new ArrayList<>();
                log.info(host + "子域名收集");
                // 子域名列表
                String cmd = String.format(Const.STR_SUBFINDER_SUBDOMAIN, toolDir, host);
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
                    mqHostSender.sendReScanHostToMqtt(host, subdomainList);

                    // 保存新增的项目-子域名关联关系
                    List<ScanProjectHostEntity> proHostEntityList = scanProjectHostDao.queryByHostList(subdomainList);
                    List<String> proHostList = proHostEntityList.stream().map(ScanProjectHostEntity::getHost).collect(Collectors.toList());
                    subdomainList.removeAll(proHostList);

                    List<ScanAddRecordEntity> recordList = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(subdomainList)) {
                        List<ScanProjectHostEntity> saveList = new ArrayList<>();
                        List<ScanProjectHostEntity> parentList = scanProjectHostDao.queryProjectByParent(host);
                        List<Long> projectIdList = parentList.stream().map(ScanProjectHostEntity::getProjectId).collect(Collectors.toList());
                        for (String s : subdomainList) {
                            for (Long projectId : projectIdList) {
                                ScanProjectHostEntity sub = ScanProjectHostEntity.builder()
                                        .parentDomain(host).host(s).projectId(projectId).isScanning(Const.INTEGER_0)
                                        .build();
                                saveList.add(sub);
                                ScanAddRecordEntity record = ScanAddRecordEntity.builder()
                                        .projectId(projectId).parentName(host).subName(s).addRecordType(Const.INTEGER_1)
                                        .build();
                                recordList.add(record);
                            }
                        }
                        scanProjectHostService.saveBatch(saveList);
                        scanAddRecordService.saveBatch(recordList);
                    }
                }
            }
        }
    }

}
