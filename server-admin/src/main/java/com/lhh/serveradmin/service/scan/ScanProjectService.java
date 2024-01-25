package com.lhh.serveradmin.service.scan;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serveradmin.feign.scan.ScanProjectContentFeign;
import com.lhh.serveradmin.feign.scan.ScanProjectFeign;
import com.lhh.serveradmin.feign.scan.ScanProjectHostFeign;
import com.lhh.serveradmin.feign.scan.ScanSecurityHoleFeign;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serveradmin.mqtt.ProjectSender;
import com.lhh.serveradmin.utils.JedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.dto.HoleNumDto;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.RexpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScanProjectService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ScanProjectFeign scanProjectFeign;
    @Autowired
    ScanProjectHostFeign scanProjectHostFeign;
    @Autowired
    ScanProjectContentFeign scanProjectContentFeign;
    @Autowired
    ScanSecurityHoleFeign scanSecurityHoleFeign;
    @Autowired
    ProjectSender projectSender;
    @Resource
    private PassJavaJwtTokenUtil jwtTokenUtil;

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
        if (hostList.size() > 2000) {
            return R.error("域名数量请保持在2000个以内！");
        }
        project.setHostList(hostList);
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
                        log.info(host + "为顶级域名，不预解析！");
                        isTop = Const.INTEGER_1;
                        isCompleted = Const.INTEGER_1;
                        isValid = false;
                    }
                    if (RexpUtil.isOtherDomain(host)) {
                        log.info(host + "包含未知顶级域名，不预解析！");
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
                        .inputHost(host).parentDomain(RexpUtil.getMajorDomain(host))
                        .scanPorts(project.getScanPorts()).isCompleted(isCompleted)
                        .isTop(isTop).unknownTop(unknownTop)
                        .build();
                saveContentList.add(content);
            }
            project = scanProjectFeign.save(project);
            for (ScanProjectContentEntity content : saveContentList) {
                content.setProjectId(project.getId());
            }
            scanProjectContentFeign.saveBatch(saveContentList);
        }
        if (!CollectionUtils.isEmpty(validHostList)) {
            project.setHostList(validHostList);
            projectSender.putProject(project);
            project.setHosts(Const.STR_EMPTY);
            JedisUtils.setJson(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()), JSON.toJSONString(project));
        } else {
            log.info("项目" + project.getId() + "输入域名包含" + JSON.toJSONString(project.getHostList()) + ",其中扫描域名包含" + JSON.toJSONString(validHostList) + ",不扫描域名包含" + JSON.toJSONString(notValidHostList));
        }
        return R.ok();
    }

    public IPage<ScanProjectEntity> page(Map<String, Object> params) {
        IPage<ScanProjectEntity> page = scanProjectFeign.basicPage(params);
        List<Long> projectIdList = page.getRecords().stream().map(ScanProjectEntity::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            for (ScanProjectEntity project : page.getRecords()) {
                String statisticsStr = redisTemplate.opsForValue().get(String.format(CacheConst.REDIS_PROJECT_STATISTICS_NUM, project.getId()));
                Map<String, Object> statisticsMap = JSON.parseObject(statisticsStr, Map.class);
                Integer portNum = CollectionUtils.isEmpty(statisticsMap) ? Const.INTEGER_0 : MapUtil.getInt(statisticsMap, "portNum");
                Integer urlNum = CollectionUtils.isEmpty(statisticsMap) ? Const.INTEGER_0 : MapUtil.getInt(statisticsMap, "urlNum");
                Integer mediumNum = CollectionUtils.isEmpty(statisticsMap) ? Const.INTEGER_0 : MapUtil.getInt(statisticsMap, "mediumNum");
                Integer highNum = CollectionUtils.isEmpty(statisticsMap) ? Const.INTEGER_0 : MapUtil.getInt(statisticsMap, "highNum");
                Integer criticalNum = CollectionUtils.isEmpty(statisticsMap) ? Const.INTEGER_0 : MapUtil.getInt(statisticsMap, "criticalNum");
                project.setPortNum(portNum);
                project.setUrlNum(urlNum);
                project.setMediumNum(mediumNum);
                project.setHighNum(highNum);
                project.setCriticalNum(criticalNum);
                String projectStr = redisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()));
                if (!StringUtils.isEmpty(projectStr)) {
                    project.setIsCompleted(Const.INTEGER_0);
                } else {
                    project.setIsCompleted(Const.INTEGER_1);
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
