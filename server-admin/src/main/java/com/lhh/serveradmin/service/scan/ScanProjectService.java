package com.lhh.serveradmin.service.scan;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serveradmin.feign.scan.ScanProjectContentFeign;
import com.lhh.serveradmin.feign.scan.ScanProjectFeign;
import com.lhh.serveradmin.feign.scan.ScanProjectHostFeign;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serveradmin.mqtt.ProjectSender;
import com.lhh.serveradmin.service.sys.SysUserService;
import com.lhh.serveradmin.utils.JedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.RexpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScanProjectService {

    @Autowired
    ScanProjectFeign scanProjectFeign;
    @Autowired
    ScanProjectHostFeign scanProjectHostFeign;
    @Autowired
    ScanProjectContentFeign scanProjectContentFeign;
    @Autowired
    ProjectSender projectSender;
    @Resource
    private PassJavaJwtTokenUtil jwtTokenUtil;

    public R test(ScanProjectEntity project) {
        projectSender.sendToMqtt2(project);
        return R.ok();
    }

    public R saveProject(ScanProjectEntity project) {
        Long userId = Long.valueOf(jwtTokenUtil.getUserId());
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("name", project.getName());
        List<ScanProjectEntity> list = scanProjectFeign.list(params);
        if (!CollectionUtils.isEmpty(list)) {
            return R.error("该项目名称已存在！");
        }
        project.setUserId(userId);
        List<String> hostList = new ArrayList<>(Arrays.asList(project.getHosts().replace(" ", "").split(Const.STR_LINEFEED)));
        project.setHostList(hostList);
        project = scanProjectFeign.save(project);
        List<ScanProjectContentEntity> saveContentList = new ArrayList<>();
        List<String> validHostList = new ArrayList<>();
        List<String> notValidHostList = new ArrayList<>();
        Boolean isValid;
        if (!CollectionUtils.isEmpty(hostList)) {
            for (String host : hostList) {
                isValid = true;
                Integer isTop = Const.INTEGER_0;
                Integer unknownTop = Const.INTEGER_0;
                Integer isCompleted = Const.INTEGER_0;
                // todo 考虑存入数据库
                if (!RexpUtil.isIP(host)) {
                    if (RexpUtil.isTopDomain(host)) {
                        log.error(host + "为顶级域名，不预解析！");
                        isTop = Const.INTEGER_1;
                        isCompleted = Const.INTEGER_1;
                        isValid = false;
                    }
                    if (RexpUtil.isOtherDomain(host)) {
                        log.error(host + "包含未知顶级域名，不预解析！");
                        unknownTop = Const.INTEGER_1;
                        isCompleted = Const.INTEGER_1;
                        isValid = false;
                    }
                }
                if (isValid) {
                    validHostList.add(host);
                } else {
                    notValidHostList.add(host);
                }
                ScanProjectContentEntity content = ScanProjectContentEntity.builder()
                        .projectId(project.getId()).inputHost(host).parentDomain(RexpUtil.getMajorDomain(host))
                        .scanPorts(project.getScanPorts()).isCompleted(isCompleted)
                        .isTop(isTop).unknownTop(unknownTop)
                        .build();
                saveContentList.add(content);
            }
            scanProjectContentFeign.saveBatch(saveContentList);
        }
        if (!CollectionUtils.isEmpty(validHostList)) {
            JedisUtils.setJson(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()), JSON.toJSONString(project));
            project.setHostList(validHostList);
            projectSender.sendToMqtt(project);
        } else {
            log.info("项目" + project.getId() + "输入域名包含" + JSON.toJSONString(project.getHostList()) + ",其中扫描域名包含" + JSON.toJSONString(validHostList) + ",不扫描域名包含" + JSON.toJSONString(notValidHostList));
        }
        return R.ok();
    }

    public IPage<ScanProjectEntity> page(Map<String, Object> params) {
        IPage<ScanProjectEntity> page = scanProjectFeign.basicPage(params);
        List<Long> projectIdList = page.getRecords().stream().map(ScanProjectEntity::getId).collect(Collectors.toList());
        List<ScanProjectEntity> numList = scanProjectFeign.getProjectPortNum(projectIdList);
        Map<Long, Integer> maps = numList.stream().collect(Collectors.toMap(ScanProjectEntity::getId, ScanProjectEntity::getPortNum));
        params.put("projectIds", projectIdList);
        List<ScanProjectContentEntity> contentList = CollectionUtils.isEmpty(projectIdList) ? new ArrayList<>() : scanProjectContentFeign.list(params);
        Map<Long, List<ScanProjectContentEntity>> contentMap = contentList.stream().collect(Collectors.groupingBy(ScanProjectContentEntity::getProjectId));
        Date now = new Date();
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            for (ScanProjectEntity project : page.getRecords()) {
                Integer portNum = maps.get(project.getId());
                project.setPortNum(portNum);
                List<ScanProjectContentEntity> allList = contentMap.containsKey(project.getId()) ? contentMap.get(project.getId()) : new ArrayList<>();
                List<ScanProjectContentEntity> scannedList = allList.stream().filter(c -> Const.INTEGER_1.equals(c.getIsCompleted())).collect(Collectors.toList());
                project.setAllHostNum(allList.size());
                project.setScannedHostNum(scannedList.size());
                Long second = DateUtil.between(project.getCreateTime(), now, DateUnit.SECOND);
                // 小于三秒，防止刚建任务就显示扫描完成
                if (second < Const.LONG_3) {
                    project.setIsCompleted(Const.INTEGER_0);
                }
            }
        }
        return page;
    }

    public List<ScanProjectEntity> list(Map<String, Object> params) {
        return scanProjectFeign.list(params);
    }

    public ScanProjectEntity info(Long id) {
        ScanProjectEntity project = scanProjectFeign.info(id);
        String hosts = project.getHosts().replace(Const.STR_COMMA, "\n");
        project.setHosts(hosts);
        return project;
    }

    public void delete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        scanProjectFeign.deleteBatch(ids);
        JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_PROJECT, ids.get(0)));
        Map<String, Object> params = new HashMap<>(Const.INTEGER_1);
        params.put("projectId", ids.get(0));
        List<ScanProjectContentEntity> contentList = scanProjectContentFeign.list(params);
        List<Long> cIds = contentList.stream().map(ScanProjectContentEntity::getId).collect(Collectors.toList());
        scanProjectContentFeign.deleteBatch(cIds);
        List<ScanProjectHostEntity> hostList = scanProjectHostFeign.list(params);
        List<Long> hIds = hostList.stream().map(ScanProjectHostEntity::getId).collect(Collectors.toList());
        scanProjectHostFeign.deleteBatch(hIds);
    }

}
