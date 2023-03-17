package com.lhh.servermonitor.controller;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.CacheConst;
import com.lhh.serverbase.utils.Const;
import com.lhh.servermonitor.dto.ScanParamDto;
import com.lhh.servermonitor.enums.OperateEnum;
import com.lhh.servermonitor.service.*;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.JedisUtils;
import com.lhh.servermonitor.utils.PortUtils;
import com.lhh.servermonitor.utils.RexpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    ScanProjectService projectService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    ExecService execService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    ScanService scanService;

    @PostMapping("saveProject")
    public void saveProject(@RequestBody ScanProjectEntity project) {
        projectService.save(project);
        projectService.saveProject(project);
    }

    @GetMapping("test")
    public SshResponse test(String cmd) {
        SshResponse response = null;
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @GetMapping("test2")
    public void test2(String host, String ports) {
        execService.getPortList(host, ports);
    }

}
