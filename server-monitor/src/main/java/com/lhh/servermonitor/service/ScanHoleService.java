package com.lhh.servermonitor.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.*;
import com.lhh.serverbase.enums.LevelEnum;
import com.lhh.serverbase.enums.ToolEnum;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.servermonitor.dao.ScanHostPortDao;
import com.lhh.servermonitor.dao.SysDictDao;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.HttpxCustomizeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScanHoleService {

    @Value("${dir-setting.tool-dir}")
    private String toolDir;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ScanPortService scanPortService;
    @Autowired
    ScanSecurityHoleService scanSecurityHoleService;
    @Autowired
    ScanAddRecordService scanAddRecordService;
    @Autowired
    SysDictService sysDictService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanHostPortDao scanHostPortDao;
    @Autowired
    ScanProjectHostService scanProjectHostService;

    /**
     * 扫描漏洞
     *
     * @param domain
     */
    public void scanHoleList(Long projectId, String domain) {
        String projectStr = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_PROJECT, projectId));
        ScanProjectEntity redisProject = JSON.parseObject(projectStr, ScanProjectEntity.class);
        List<Integer> portList = scanPortService.queryPortList(domain);
        if (!CollectionUtils.isEmpty(portList) && !StringUtils.isEmpty(redisProject)) {
            if (Const.INTEGER_1.equals(redisProject.getNucleiFlag())) {
                nucleiAllScan(projectId, domain, portList, ToolEnum.nuclei.getToolType(), redisProject.getNucleiParams());
            }
            if (Const.INTEGER_1.equals(redisProject.getAfrogFlag())) {
                afrogAllScan(projectId, domain, portList, ToolEnum.afrog.getToolType(), redisProject.getAfrogParams());
            }
            if (Const.INTEGER_1.equals(redisProject.getXrayFlag())) {
                xrayAllScan(projectId, domain, portList, ToolEnum.xray.getToolType(), redisProject.getXrayParams());
            }
        }
    }

    /**
     * 扫描漏洞
     *
     */
    public void scanHoleSingle(Long projectId, String domain, Integer port) {
        String requestUrl = domain + Const.STR_COLON + port;
        String projectStr = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_PROJECT, projectId));
        ScanProjectEntity redisProject = JSON.parseObject(projectStr, ScanProjectEntity.class);
        if (!StringUtils.isEmpty(redisProject)) {
            if (Const.INTEGER_1.equals(redisProject.getNucleiFlag())) {
                nucleiSingleScan(projectId, domain, requestUrl, ToolEnum.nuclei.getToolType(), redisProject.getNucleiParams());
            }
            if (Const.INTEGER_1.equals(redisProject.getAfrogFlag())) {
                nucleiSingleScan(projectId, domain, requestUrl, ToolEnum.afrog.getToolType(), redisProject.getAfrogParams());
            }
            if (Const.INTEGER_1.equals(redisProject.getXrayFlag())) {
                xraySingleScan(projectId, domain, port, ToolEnum.xray.getToolType(), redisProject.getXrayParams());
            }
        }
    }

    public void nucleiSingleScan(Long projectId, String domain, String requestUrl, Integer tool, String param) {
        log.info("项目" + projectId + Const.STR_COLON + requestUrl + "---nuclei漏洞扫描开始");
        String query = "-s";
        String notQuery = "-es";
        List<String> initLevelList = new ArrayList<>(Arrays.asList("medium", "high", "critical"));
        List<String> initNotLevelList = new ArrayList<>(Arrays.asList("info", "unknown", "low"));
        Map<String, Object> map = getHoleParams(param, query, notQuery, initLevelList, initNotLevelList);
        param = MapUtil.getStr(map, "param");
        List<String> levelList = (List<String>) map.get("levelList");
        if (CollectionUtils.isEmpty(levelList)) {
            log.error("项目" + projectId + Const.STR_COLON + requestUrl + "扫描漏洞等级不能为空，输入参数：" + param);
            return;
        }
        SshResponse response = null;
        String cmd = String.format(Const.STR_NUCLEI, toolDir, requestUrl, param);
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            log.error("项目" + projectId + Const.STR_COLON + requestUrl + "扫描参数异常，输入参数：" + e);
            e.printStackTrace();
        }
        String outStr = RexpUtil.removeColor(response.getOut());
        nucleiScan(projectId, domain, tool, outStr, levelList, cmd);
        log.info("项目" + projectId + Const.STR_COLON + domain + "---nuclei漏洞扫描结束");
    }

    public void nucleiAllScan(Long projectId, String domain, List<Integer> portList, Integer tool, String param) {
        log.info("项目" + projectId + Const.STR_COLON + domain + "---nuclei漏洞扫描开始");
        String query = "-s";
        String notQuery = "-es";
        List<String> initLevelList = new ArrayList<>(Arrays.asList("medium", "high", "critical"));
        List<String> initNotLevelList = new ArrayList<>(Arrays.asList("info", "unknown", "low"));
        Map<String, Object> map = getHoleParams(param, query, notQuery, initLevelList, initNotLevelList);
        param = MapUtil.getStr(map, "param");
        List<String> levelList = (List<String>) map.get("levelList");
        if (CollectionUtils.isEmpty(levelList)) {
            log.error("项目" + projectId + Const.STR_COLON + domain + "扫描漏洞等级不能为空，输入参数：" + param);
            return;
        }

        List<String> urlList = new ArrayList<>();
        for (Integer port : portList) {
            urlList.add(domain + Const.STR_COLON + port);
        }
        log.info("项目" + projectId + Const.STR_COLON + domain + "-nuclei扫描对象：" + JSON.toJSONString(urlList));
        String urls = String.join("\\\\" + "n", urlList);
        SshResponse response = null;
        String cmd = String.format(Const.STR_NUCLEI_LIST, toolDir, projectId + Const.STR_UNDERLINE + domain, param);
        try {
            ExecUtil.runCommand(String.format(Const.STR_CREATE_URLS, toolDir, toolDir+ToolEnum.nuclei.getTool(), urls, projectId + Const.STR_UNDERLINE + domain));
            response = ExecUtil.runCommand(cmd);
            ExecUtil.runCommand(String.format(Const.STR_DEL_URLS, toolDir+ToolEnum.nuclei.getTool() + Const.STR_SLASH + "urls", projectId + Const.STR_UNDERLINE + domain));
        } catch (IOException e) {
            log.error("项目" + projectId + Const.STR_COLON + domain + "扫描参数异常，输入参数：" + e);
            e.printStackTrace();
        }
        String outStr = RexpUtil.removeColor(response.getOut());
        log.info("项目" + projectId + Const.STR_COLON + domain + "---nuclei漏洞扫描响应：" + response.getOut());
        nucleiScan(projectId, domain, tool, outStr, levelList, cmd);
        log.info("项目" + projectId + Const.STR_COLON + domain + "---nuclei漏洞扫描结束");
    }

    public void nucleiScan(Long projectId, String domain, Integer tool, String reponseStr, List<String> levelList, String cmd) {
        String majorDomain = RexpUtil.getMajorDomain(domain);
        List<String> responseLineList = Arrays.asList(reponseStr.split("\n"));
        List<String> serverLineList = responseLineList.stream().filter(r -> r.startsWith("[") && !r.startsWith("[INF]")).collect(Collectors.toList());
        List<ScanSecurityHoleEntity> holeList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(serverLineList)) {
            for (String str : serverLineList) {
                String[] list = str.split(Const.STR_BLANK);
                String holeName = list[0].substring(1, list[0].length() - 1);
                String protocol = list[1].substring(1, list[1].length() - 1);
                String level = list[2].substring(1, list[2].length() - 1);
                String url = list[3];
                String describe = list.length > 4 ? list[4].substring(1, list[4].length() - 1) : Const.STR_CROSSBAR;
                ScanSecurityHoleEntity hole = ScanSecurityHoleEntity.builder()
                        .projectId(projectId)
                        .domain(majorDomain).subDomain(domain)
                        .name(holeName).level(LevelEnum.getLevel(level))
                        .protocol(protocol).url(url).info(describe)
                        .status(Const.INTEGER_1).toolType(tool)
                        .build();
                if (Const.INTEGER_MINUS_1.equals(url.indexOf(Const.STR_QUESTION))) {
                    hole.setPreUrl(url);
                } else {
                    hole.setPreUrl(url.substring(Const.INTEGER_0, url.indexOf(Const.STR_QUESTION)));
                }
                holeList.add(hole);
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("subDomain", domain);
        params.put("statusList", Arrays.asList(Const.INTEGER_1, Const.INTEGER_3));
        params.put("toolType", ToolEnum.nuclei.getToolType());
        List<Integer> levelTypeList = new ArrayList<>();
        levelList.stream().forEach(l->levelTypeList.add(LevelEnum.getLevel(l)));
        params.put("levelList", levelTypeList);
        List<ScanSecurityHoleEntity> oldList = scanSecurityHoleService.basicList(params);
        List<ScanSecurityHoleEntity> sameList = holeList.stream().filter(item -> oldList.contains(item)).collect(Collectors.toList());
        oldList.removeAll(sameList);
        holeList.removeAll(sameList);
        if (!CollectionUtils.isEmpty(oldList)) {
            for (ScanSecurityHoleEntity hole : oldList) {
                hole.setStatus(Const.INTEGER_2);
                // 修复时，存储当前扫描命令
                hole.setRemark(cmd);
                scanSecurityHoleService.updateById(hole);
            }
        }
        if (!CollectionUtils.isEmpty(holeList)) {
            scanSecurityHoleService.saveBatch(holeList);
            List<ScanAddRecordEntity> recordList = new ArrayList<>();
            for (ScanSecurityHoleEntity hole : holeList) {
                // 新增扫描端口记录
                String target = getUrlTarget(hole.getUrl());
                ScanAddRecordEntity record = ScanAddRecordEntity.builder()
                        .projectId(projectId).parentName(target)
                        .subName(hole.getName()).addRecordType(Const.INTEGER_3)
                        .build();
                recordList.add(record);
            }
            if (!CollectionUtils.isEmpty(recordList)) {
                scanAddRecordService.saveBatch(recordList);
            }
        }
    }

    public void afrogSingleScan(Long projectId, String domain, String requestUrl, Integer tool, String param) {
        log.info("项目" + projectId + Const.STR_COLON + requestUrl + "---afrog漏洞扫描开始");
        String query = "-S";
        String notQuery = Const.STR_EMPTY;
        List<String> initLevelList = new ArrayList<>(Arrays.asList("medium", "high", "critical"));
        List<String> initNotLevelList = new ArrayList<>(Arrays.asList("info", "unknown", "low"));
        Map<String, Object> map = getHoleParams(param, query, notQuery, initLevelList, initNotLevelList);
        param = MapUtil.getStr(map, "param");
        List<String> levelList = (List<String>) map.get("levelList");
        if (CollectionUtils.isEmpty(levelList)) {
            log.error("项目" + projectId + Const.STR_COLON + requestUrl + "扫描漏洞等级不能为空，输入参数：" + param);
            return;
        }
        SshResponse response = null;
        String cmd = String.format(Const.STR_AFROG, toolDir, requestUrl, param);
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            log.error("项目" + projectId + Const.STR_COLON + requestUrl + "扫描参数异常，输入参数：" + e);
            e.printStackTrace();
        }
        String outStr = RexpUtil.removeColor(response.getOut());
        afrogScan(projectId, domain, tool, outStr, levelList, cmd);
        log.info("项目" + projectId + Const.STR_COLON + requestUrl + "---afrog漏洞扫描结束");
    }

    public void afrogAllScan(Long projectId, String domain, List<Integer> portList, Integer tool, String param) {
        log.info("项目" + projectId + Const.STR_COLON + domain + "---afrog漏洞扫描开始");
        String query = "-S";
        String notQuery = Const.STR_EMPTY;
        List<String> initLevelList = new ArrayList<>(Arrays.asList("medium", "high", "critical"));
        List<String> initNotLevelList = new ArrayList<>(Arrays.asList("info", "unknown", "low"));
        Map<String, Object> map = getHoleParams(param, query, notQuery, initLevelList, initNotLevelList);
        param = MapUtil.getStr(map, "param");
        List<String> levelList = (List<String>) map.get("levelList");
        if (CollectionUtils.isEmpty(levelList)) {
            log.error("项目" + projectId + Const.STR_COLON + domain + "扫描漏洞等级不能为空，输入参数：" + param);
            return;
        }

        List<String> urlList = new ArrayList<>();
        for (Integer port : portList) {
            urlList.add(domain + Const.STR_COLON + port);
        }
        log.info("项目" + projectId + Const.STR_COLON + domain + "-afrog扫描对象：" + JSON.toJSONString(urlList));
        String urls = String.join("\\\\" + "n", urlList);
        SshResponse response = null;
        String cmd = String.format(Const.STR_AFROG_LIST, toolDir, projectId + Const.STR_UNDERLINE + domain, param);
        try {
            ExecUtil.runCommand(String.format(Const.STR_CREATE_URLS, toolDir, toolDir+ToolEnum.afrog.getTool(), urls, projectId + Const.STR_UNDERLINE + domain));
            response = ExecUtil.runCommand(cmd);
            ExecUtil.runCommand(String.format(Const.STR_DEL_URLS, toolDir+ToolEnum.afrog.getTool() + Const.STR_SLASH + "urls", projectId + Const.STR_UNDERLINE + domain));
        } catch (IOException e) {
            log.error("项目" + projectId + Const.STR_COLON + domain + "扫描参数异常，输入参数：" + e);
            e.printStackTrace();
        }
        String outStr = RexpUtil.removeColor(response.getOut());
        log.info("项目" + projectId + Const.STR_COLON + domain + "---afrog漏洞扫描响应：" + response.getOut());
        afrogScan(projectId, domain, tool, outStr, levelList, cmd);
        log.info("项目" + projectId + Const.STR_COLON + domain + "---afrog漏洞扫描结束");
    }

    public void afrogScan(Long projectId, String domain, Integer tool, String reponseStr, List<String> levelList, String cmd) {
        String majorDomain = RexpUtil.getMajorDomain(domain);
        List<String> responseLineList = Arrays.asList(reponseStr.split(Const.STR_LINEFEED));
        List<String> serverLineList = new ArrayList<>();
        Boolean beginFlag = false;
        if (!CollectionUtils.isEmpty(responseLineList)) {
            for (String s : responseLineList) {
                if (s.startsWith("0%")) {
                    beginFlag = true;
                }
                if (!beginFlag) {
                    continue;
                }
                if (Const.STR_LINEFEED.equals(s) || Const.STR_EMPTY.equals(s)) {
                    continue;
                }
                String [] arr = s.split(Const.STR_BLANK);
                String tmp = Const.STR_EMPTY;
                if (arr != null && arr.length != 0) {
                    tmp = arr[0];
                }
                if (!tmp.equals(Const.STR_EMPTY) && !tmp.contains(Const.STR_PERCENT)) {
                    serverLineList.add(s);
                }
            }
        }
        List<ScanSecurityHoleEntity> holeList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(serverLineList)) {
            for (String str : serverLineList) {
                String[] list = str.split(Const.STR_BLANK);
                String holeName = list[3];
                String protocol = Const.STR_CROSSBAR;
                String level = list[4];
                String url = list[5];
                String describe = Const.STR_CROSSBAR;
                ScanSecurityHoleEntity hole = ScanSecurityHoleEntity.builder()
                        .projectId(projectId)
                        .domain(majorDomain).subDomain(domain)
                        .name(holeName).level(LevelEnum.getLevel(level))
                        .protocol(protocol).url(url).info(describe)
                        .status(Const.INTEGER_1).toolType(tool)
                        .build();
                if (Const.INTEGER_MINUS_1.equals(url.indexOf(Const.STR_QUESTION))) {
                    hole.setPreUrl(url);
                } else {
                    hole.setPreUrl(url.substring(Const.INTEGER_0, url.indexOf(Const.STR_QUESTION)));
                }
                holeList.add(hole);
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("subDomain", domain);
        params.put("status", Const.INTEGER_1);
        params.put("toolType", ToolEnum.afrog.getToolType());
        List<Integer> levelTypeList = new ArrayList<>();
        levelList.stream().forEach(l->levelTypeList.add(LevelEnum.getLevel(l)));
        params.put("levelList", levelTypeList);
        List<ScanSecurityHoleEntity> oldList = scanSecurityHoleService.basicList(params);
        List<ScanSecurityHoleEntity> sameList = holeList.stream().filter(item -> oldList.contains(item)).collect(Collectors.toList());
        oldList.removeAll(sameList);
        holeList.removeAll(sameList);
        if (!CollectionUtils.isEmpty(oldList)) {
            for (ScanSecurityHoleEntity hole : oldList) {
                hole.setStatus(Const.INTEGER_2);
                hole.setRemark(cmd);
                scanSecurityHoleService.updateById(hole);
            }
        }
        if (!CollectionUtils.isEmpty(holeList)) {
            scanSecurityHoleService.saveBatch(holeList);
            List<ScanAddRecordEntity> recordList = new ArrayList<>();
            for (ScanSecurityHoleEntity hole : holeList) {
                // 新增扫描端口记录
                String target = getUrlTarget(hole.getUrl());
                ScanAddRecordEntity record = ScanAddRecordEntity.builder()
                        .projectId(projectId).parentName(target)
                        .subName(hole.getName()).addRecordType(Const.INTEGER_3)
                        .build();
                recordList.add(record);
            }
            if (!CollectionUtils.isEmpty(recordList)) {
                scanAddRecordService.saveBatch(recordList);
            }
        }
    }

    public static Map<String, Object> getHoleParams(String params, String query, String notQuery, List<String> levelList, List<String> notLevelList) {
        params = params.replaceAll("\\s+", " ");
        String[] arr = params.split(Const.STR_BLANK);
        for (int i = 0; i < arr.length; i++) {
            if (!StringUtils.isEmpty(query) && query.equals(arr[i])) {
                levelList = new ArrayList<>(Arrays.asList(arr[i + 1].split(Const.STR_COMMA)));
                arr[i] = Const.STR_EMPTY;
                arr[i + 1] = Const.STR_EMPTY;
            }
            if (!StringUtils.isEmpty(notQuery) && notQuery.equals(arr[i])) {
                notLevelList = new ArrayList<>(Arrays.asList(arr[i + 1].split(Const.STR_COMMA)));
                arr[i] = Const.STR_EMPTY;
                arr[i + 1] = Const.STR_EMPTY;
            }
        }
        levelList.removeAll(notLevelList);
        List<String> list = new ArrayList<>(Arrays.asList(arr));
        if (!CollectionUtils.isEmpty(levelList)) {
            list.add(query);
            list.add(String.join(Const.STR_COMMA, levelList));
        }
        String result = String.join(Const.STR_BLANK, list).replaceAll("\\s+", " ");
        Map<String, Object> map = new HashMap<>();
        map.put("param", result);
        map.put("levelList", levelList);
        return map;
    }

    public void xraySingleScan(Long projectId, String domain, Integer port, Integer tool, String param) {
        log.info("项目" + projectId + Const.STR_COLON + domain + Const.STR_COLON + port + "---xray漏洞扫描开始");
        String requestUrl;
        requestUrl = domain + Const.STR_COLON + port;
        log.info("项目" + projectId + Const.STR_COLON + domain + "-xray扫描对象：" + requestUrl);
        SshResponse response = null;
        String cmd = String.format(Const.STR_XRAY, toolDir, requestUrl, param);
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            log.error("项目" + projectId + Const.STR_COLON + requestUrl + "扫描参数异常，输入参数：" + e);
        }
        String outStr = RexpUtil.removeColor(response.getOut());
        xrayScan(projectId, domain, tool, outStr, cmd);
        log.info("项目" + projectId + Const.STR_COLON + domain + Const.STR_COLON + port + "---xray漏洞扫描结束");
    }

    public void xrayAllScan(Long projectId, String domain, List<Integer> portList, Integer tool, String param) {
        log.info("项目" + projectId + Const.STR_COLON + domain + "---xray漏洞扫描开始");
        String requestUrl;
        for(Integer port : portList) {
            requestUrl = domain + Const.STR_COLON + port;
            log.info("项目" + projectId + Const.STR_COLON + domain + "-xray扫描对象：" + requestUrl);
            SshResponse response = null;
            String cmd = String.format(Const.STR_XRAY, toolDir, requestUrl, param);
            try {
                response = ExecUtil.runCommand(cmd);
            } catch (IOException e) {
                log.error("项目" + projectId + Const.STR_COLON + requestUrl + "扫描参数异常，输入参数：" + e);
            }
            String outStr = RexpUtil.removeColor(response.getOut());
            xrayScan(projectId, domain, tool, outStr, cmd);
        }
        log.info("项目" + projectId + Const.STR_COLON + domain + "---xray漏洞扫描结束");
    }

    public void xrayScan(Long projectId, String domain, Integer tool, String reponseStr, String cmd) {
        String majorDomain = RexpUtil.getMajorDomain(domain);
        Map<Integer, ScanSecurityHoleEntity> tmpMap = new HashMap<>();
        List<String> responseLineList = Arrays.asList(reponseStr.split(Const.STR_LINEFEED));
        Boolean holeFlag = false;
        Integer num = Const.INTEGER_0;
        if(!CollectionUtils.isEmpty(responseLineList)) {
            for (String s : responseLineList) {
                if (s.startsWith("[Vuln: ") && !s.contains("baseline")) {
                    num++;
                    holeFlag = true;
                    continue;
                }
                if (Const.STR_LINEFEED.equals(s) || Const.STR_EMPTY.equals(s)) {
                    holeFlag = false;
                }
                if (holeFlag) {
                    ScanSecurityHoleEntity hole = tmpMap.get(num) == null ? ScanSecurityHoleEntity.builder()
                            .projectId(projectId)
                            .domain(majorDomain)
                            .subDomain(domain)
                            .protocol(Const.STR_CROSSBAR)
                            .info(Const.STR_CROSSBAR)
                            .status(Const.INTEGER_1)
                            .toolType(tool)
                            .build() : tmpMap.get(num);
                    String key = s.substring(0, s.indexOf(Const.STR_BLANK));
                    String value = s.substring(s.indexOf("\"")+1, s.lastIndexOf("\""));
                    if ("Target".equals(key)) {
                        hole.setUrl(value);
                        if (Const.INTEGER_MINUS_1.equals(value.indexOf(Const.STR_QUESTION))) {
                            hole.setPreUrl(value);
                        } else {
                            hole.setPreUrl(value.substring(Const.INTEGER_0, value.indexOf(Const.STR_QUESTION)));
                        }
                        tmpMap.put(num, hole);
                    }
                    if ("VulnType".equals(key)) {
                        hole.setName(value);
                        tmpMap.put(num, hole);
                    }
                    if ("level".equals(key)) {
                        hole.setLevel(LevelEnum.getLevel(value));
                        tmpMap.put(num, hole);
                    }
                }
            }
        }
        List<ScanSecurityHoleEntity> holeList = new ArrayList<>(tmpMap.values());

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("subDomain", domain);
        params.put("status", Const.INTEGER_1);
        params.put("toolType", ToolEnum.xray.getToolType());
        List<Integer> levelTypeList = holeList.stream().map(ScanSecurityHoleEntity::getLevel).distinct().collect(Collectors.toList());
        params.put("levelList", levelTypeList);
        List<ScanSecurityHoleEntity> oldList = scanSecurityHoleService.basicList(params);
        List<ScanSecurityHoleEntity> sameList = holeList.stream().filter(item -> oldList.contains(item)).collect(Collectors.toList());
        oldList.removeAll(sameList);
        holeList.removeAll(sameList);
        if (!CollectionUtils.isEmpty(oldList)) {
            for (ScanSecurityHoleEntity hole : oldList) {
                hole.setStatus(Const.INTEGER_2);
                hole.setRemark(cmd);
                scanSecurityHoleService.updateById(hole);
            }
        }
        if (!CollectionUtils.isEmpty(holeList)) {
            scanSecurityHoleService.saveBatch(holeList);
            List<ScanAddRecordEntity> recordList = new ArrayList<>();
            for (ScanSecurityHoleEntity hole : holeList) {
                // 新增扫描端口记录
                String target = getUrlTarget(hole.getUrl());
                ScanAddRecordEntity record = ScanAddRecordEntity.builder()
                        .projectId(projectId).parentName(target)
                        .subName(hole.getName()).addRecordType(Const.INTEGER_3)
                        .build();
                recordList.add(record);
            }
            if (!CollectionUtils.isEmpty(recordList)) {
                scanAddRecordService.saveBatch(recordList);
            }
        }
    }

    public String getUrlTarget(String requestUrl){
        URL url = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        int port = url.getPort();
        String portString = (port == -1) ? "" : (":" + port);
        String urlWithoutPort = url.getProtocol() + "://" + url.getHost();
        String target = urlWithoutPort + portString;
        return target;
    }

}
