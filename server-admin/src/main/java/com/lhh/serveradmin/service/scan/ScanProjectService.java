package com.lhh.serveradmin.service.scan;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serveradmin.feign.scan.ScanProjectContentFeign;
import com.lhh.serveradmin.feign.scan.ScanProjectFeign;
import com.lhh.serveradmin.feign.scan.ScanProjectHostFeign;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serveradmin.mqtt.ProjectSender;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
        List<String> hostList = new ArrayList<>(Arrays.asList(project.getHosts().split(Const.STR_COMMA)));
        project.setHostList(hostList);
        project = scanProjectFeign.save(project);
        List<ScanProjectContentEntity> saveContentList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(project.getHostList())) {
            for (String host : project.getHostList()) {
                ScanProjectContentEntity content = ScanProjectContentEntity.builder()
                        .projectId(project.getId()).inputHost(host)
                        .scanPorts(project.getScanPorts()).isCompleted(Const.INTEGER_0)
                        .build();
                saveContentList.add(content);
            }
            scanProjectContentFeign.saveBatch(saveContentList);
        }
        projectSender.sendToMqtt(project);
        return R.ok();
    }

    public IPage<ScanProjectEntity> page(Map<String, Object> params) {
        IPage<ScanProjectEntity> page = scanProjectFeign.page(params);
        List<Long> projectIds = page.getRecords().stream().map(ScanProjectEntity::getId).collect(Collectors.toList());
        params.put("projectIds", projectIds);
        List<ScanProjectContentEntity> contentList = CollectionUtils.isEmpty(projectIds) ? new ArrayList<>() : scanProjectContentFeign.list(params);
        Map<Long, List<ScanProjectContentEntity>> contentMap = contentList.stream().collect(Collectors.groupingBy(ScanProjectContentEntity::getProjectId));
        Date now = new Date();
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            for (ScanProjectEntity project : page.getRecords()) {
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

    public ScanProjectEntity info(Long id) {
        ScanProjectEntity project = scanProjectFeign.info(id);
        return project;
    }

    public void delete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        scanProjectFeign.deleteBatch(ids);
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
