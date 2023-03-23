package com.lhh.servermonitor.controller;

import com.lhh.serverbase.entity.*;
import com.lhh.servermonitor.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DataController<T> {

    @Autowired
    ScanProjectService scanProjectService;
    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanPortService scanPortService;

    @GetMapping("createData")
    public void createData(Integer count) {
        List<ScanProjectHostEntity> phList = new ArrayList<>();
        List<ScanHostEntity> hostList = new ArrayList<>();
        List<ScanPortEntity> portList = new ArrayList<>();
        Long num = 1L;
        for (int i = 1; i <= count; i++) {
            if (i % 10 == 0 || i == count) {
                ScanProjectEntity project = ScanProjectEntity.builder()
                        .userId(1L).name("project" + num).scanPorts("1000-2000")
                        .build();
                num++;
                scanProjectService.save(project);
            }
            ScanProjectHostEntity ph = ScanProjectHostEntity.builder()
                    .projectId(num).host(i + ".son.com")
                    .build();
            phList.add(ph);
            ScanHostEntity host = ScanHostEntity.builder()
                    .domain(i + ".son.com").parentDomain(i + ".parent.com").ip("192.168.1." + i).type(1)
                    .build();
            hostList.add(host);

            for (int j = 1; j <= 10; j++) {
                for (int k = 1; k <= 5; k++) {
                    ScanPortEntity port = ScanPortEntity.builder()
                            .ip("192.168.1." + (j + (i - 1) * 10)).port(1000 + k).serverName("服务" + (1000 + k))
                            .build();
                    portList.add(port);
                }
            }
            if (i%200==0) {
                scanProjectHostService.saveBatch(phList);
                phList.clear();
                scanHostService.saveBatch(hostList);
                hostList.clear();
                scanPortService.saveBatch(portList);
                portList.clear();
            }
        }
    }

}
