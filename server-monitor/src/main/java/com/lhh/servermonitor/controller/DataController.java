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
    ScanHostIpService scanHostIpService;
    @Autowired
    ScanPortService scanPortService;

    @GetMapping("createData")
    public void createData(Integer count) {
        List<ScanProjectHostEntity> phList = new ArrayList<>();
        List<ScanHostEntity> hostList = new ArrayList<>();
        List<ScanHostIpEntity> hostIpList = new ArrayList<>();
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
                    .host(i + ".son.com").parentHost(i + ".parent.com").type(1)
                    .build();
            hostList.add(host);

            for (int j = 1; j <= 10; j++) {
                ScanHostIpEntity hostIp = ScanHostIpEntity.builder()
                        .host(i + ".son.com").ip("192.168.1." + (j + (i - 1) * 10))
                        .build();
                hostIpList.add(hostIp);

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
                scanHostIpService.saveBatch(hostIpList);
                hostIpList.clear();
                scanPortService.saveBatch(portList);
                portList.clear();
            }
        }

//        List<List<ScanProjectHostEntity>> list1 = getList(phList);
//        List<List<ScanHostEntity>> list2 = getList(hostList);
//        List<List<ScanHostIpEntity>> list3 = getList(hostIpList);
//        List<List<ScanPortEntity>> list4 = getList(portList);

        /*for (List<ScanProjectHostEntity> list : list1) {
            scanProjectHostService.saveBatch(list);
        }
        for (List<ScanHostEntity> list : list2) {
            scanHostService.saveBatch(list);
        }
        for (List<ScanHostIpEntity> list : list3) {
            scanHostIpService.saveBatch(list);
        }
        for (List<ScanPortEntity> list : list4) {
            scanPortService.saveBatch(list);
        }*/
    }

    public <T> List<List<T>> getList(List<T> list){
        Integer start = 0;
        List<List<T>> resultList = new ArrayList<>();
        for (int i = 1; i <= list.size();i++) {
            if (i%5000==0) {
                List<T> result = list.subList(start, i-1);
                resultList.add(result);
                start = i;
            }
            if (i == list.size()-1) {
                List<T> result = list.subList(start, list.size()-1);
                resultList.add(result);
                break;
            }
        }
        return resultList;
    }
}
