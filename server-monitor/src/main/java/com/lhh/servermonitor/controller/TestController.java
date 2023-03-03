package com.lhh.servermonitor.controller;

import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.servermonitor.service.ExecService;
import com.lhh.servermonitor.utils.RexpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    ExecService execService;

    @PostMapping("saveProject")
    public void saveProject(@RequestBody ScanProjectEntity project){
        if (!CollectionUtils.isEmpty(project.getHostList())) {
            List<String> ipList = project.getHostList().stream().filter(i-> RexpUtil.isIP(i)).collect(Collectors.toList());
            List<String> hostList = project.getHostList().stream().filter(item -> !ipList.contains(item)).collect(Collectors.toList());

            execService.test(ipList, project.getPorts());
           /* for (String ip : ipList) {
                execService.getPortList(ip, project.getPorts());
            }

            for (String host : hostList) {
                List<String> newIpList = execService.getDomainList(host);
                for (String ip : newIpList) {
                    execService.getPortList(ip, project.getPorts());
                }
            }*/
        }
    }

    @GetMapping("test")
    public void test(String domain){
        execService.getDomainIpList(Arrays.asList(domain));
    }

    @GetMapping("test2")
    public void test2(String host, String ports){
        execService.getPortList(host, ports);
    }

}
