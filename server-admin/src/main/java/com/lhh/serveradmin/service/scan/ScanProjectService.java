package com.lhh.serveradmin.service.scan;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serveradmin.feign.scan.ScanProjectContentFeign;
import com.lhh.serveradmin.feign.scan.ScanProjectFeign;
import com.lhh.serveradmin.feign.scan.ScanProjectHostFeign;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serveradmin.mqtt.ProjectSender;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.common.response.R;
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

    public R saveProject(ScanProjectEntity project) {
        Long userId = Long.valueOf(jwtTokenUtil.getUserId());
        project.setUserId(userId);
        List<String> hostList = new ArrayList<>(Arrays.asList(project.getHosts().split(Const.STR_COMMA)));
        project.setHostList(hostList);
        project = scanProjectFeign.save(project);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("name", project.getName());
        List<ScanProjectEntity> list = scanProjectFeign.list(params);
        if (!CollectionUtils.isEmpty(list)) {
            R.error("该项目名称已存在！");
        }
        projectSender.sendToMqtt(project);
        return R.ok();
    }

    public IPage<ScanProjectEntity> page(Map<String, Object> params) {
        params.put("userId", jwtTokenUtil.getUserId());
        IPage<ScanProjectEntity> page = scanProjectFeign.page(params);
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
