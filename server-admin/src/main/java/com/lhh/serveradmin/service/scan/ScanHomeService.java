package com.lhh.serveradmin.service.scan;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serveradmin.feign.scan.*;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serveradmin.utils.JedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.dto.GroupTagDto;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.dto.KeyValueDto;
import com.lhh.serverbase.entity.ScanAddRecordEntity;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.RexpUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScanHomeService {

    @Autowired
    ScanProjectFeign scanProjectFeign;
    @Autowired
    ScanProjectContentFeign scanProjectContentFeign;
    @Autowired
    ScanHostFeign scanHostFeign;
    @Autowired
    ScanPortFeign scanPortFeign;
    @Autowired
    ScanSecurityHoleFeign scanSecurityHoleFeign;
    @Autowired
    ScanAddRecordFeign scanAddRecordFeign;
    @Resource
    private PassJavaJwtTokenUtil jwtTokenUtil;

    public Map<String, Object> getHomeNum(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Integer type = MapUtil.getInt(params, "type");
        switch (type) {
            case 1:
                List<ScanProjectEntity> projectList = scanProjectFeign.list(params);
                result = new HashMap<String, Object>(){{put("title", "项目");put("num", projectList.size());put("type", type);}};
                break;
            case 2:
                Integer companyNum = scanHostFeign.getCompanyNum(params);
                result = new HashMap<String, Object>(){{put("title", "企业");put("num", companyNum);put("type", type);}};
                break;
            case 3:
                Integer primaryDomainNum = scanHostFeign.getDomainNum(params);
                result = new HashMap<String, Object>(){{put("title", "主域名");put("num", primaryDomainNum);put("type", type);}};
                break;
            case 4:
                Integer subDomainNum = scanHostFeign.getSubDomainNum(params);
                result = new HashMap<String, Object>(){{put("title", "子域名");put("num", subDomainNum);put("type", type);}};
                break;
            case 5:
                Integer ipNum = scanPortFeign.getGroupTagNum(params);
                result = new HashMap<String, Object>(){{put("title", "IP");put("num", ipNum);put("type", type);}};
                break;
            case 6:
                HomeNumDto numDto = scanPortFeign.getHomeNum(params);
                result = new HashMap<String, Object>(){{put("title", "端口");put("num", numDto.getPortNum());put("type", 6);}};
                break;
            case 7:
                numDto = scanPortFeign.getHomeNum(params);
                result = new HashMap<String, Object>(){{put("title", "网站");put("num", numDto.getUrlNum());put("type", type);}};
                break;
            case 8:
                numDto = scanSecurityHoleFeign.getHomeNum(params);
                result = new HashMap<String, Object>(){{put("title", "漏洞");put("num", numDto.getHoleNum());put("type", type);}};
                break;
            case 9:
                List<ScanProjectContentEntity> contentList = scanProjectContentFeign.list(params);
                List<ScanProjectContentEntity> completeList = contentList.stream().filter(i->Const.INTEGER_1.equals(i.getIsCompleted())).collect(Collectors.toList());
                List<ScanProjectContentEntity> completeDomainList = completeList.stream().filter(i->!RexpUtil.isIP(i.getInputHost())).collect(Collectors.toList());
                result = new HashMap<String, Object>(){{put("title", "主域名已收集");put("num", completeDomainList.size());put("type", type);}};
                break;
            case 10:
                contentList = scanProjectContentFeign.list(params);
                completeList = contentList.stream().filter(i->Const.INTEGER_1.equals(i.getIsCompleted())).collect(Collectors.toList());
                List<ScanProjectContentEntity> completeIpList = completeList.stream().filter(i->RexpUtil.isIP(i.getInputHost())).collect(Collectors.toList());
                result = new HashMap<String, Object>(){{put("title", "IP已扫描");put("num", completeIpList.size());put("type", type);}};
                break;
        }
        return result;
    }

    public List<Map<String, Object>> getUnHomeNum(Map<String, Object> params) {
        Integer dNum = Const.INTEGER_0;
        Integer iNum = Const.INTEGER_0;
        Long userId = Long.valueOf(jwtTokenUtil.getUserId());
        params.put("userId", userId);
        List<ScanProjectEntity> proList = scanProjectFeign.list(params);
        if (!CollectionUtils.isEmpty(proList)) {
            for (ScanProjectEntity project : proList) {
                String projectStr = JedisUtils.getStr(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()));
                if (!StringUtils.isEmpty(projectStr)) {
                    ScanProjectEntity redisProject = JSON.parseObject(projectStr, ScanProjectEntity.class);
                    List<String> hostList = CollectionUtils.isEmpty(redisProject.getHostList()) ? new ArrayList<>() : redisProject.getHostList();
                    for (String host : hostList) {
                        if (RexpUtil.isIP(host)) {
                            iNum++;
                        } else {
                            dNum++;
                        }
                    }
                }
            }
        }
        Integer finalDNum = dNum;
        Map<String, Object> dResult = new HashMap<String, Object>(){{put("title", "主域名收集");put("num", finalDNum);}};
        Integer finalINum = iNum;
        Map<String, Object> iResult = new HashMap<String, Object>(){{put("title", "IP扫描");put("num", finalINum);}};
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(dResult);
        list.add(iResult);
        return list;
    }

    public List<ScanAddRecordEntity> getRecordList(Map<String, Object> params) {
        List<ScanAddRecordEntity> recordList = scanAddRecordFeign.list(params);
        String describe = Const.STR_EMPTY;
        if (!CollectionUtils.isEmpty(recordList)) {
            for (ScanAddRecordEntity record : recordList) {
                switch (record.getAddRecordType()) {
                    case 1:
                        describe = record.getParentName() + "  新增子域名  " + record.getSubName();
                        break;
                    case 2:
                        describe = IpLongUtils.longToIp(Long.valueOf(record.getParentName())) + "  新增开放端口  " + record.getSubName();
                        break;
                    default:
                        describe = record.getParentName() + "  检测出  " + record.getSubName();
                }
                record.setDescribe(describe);
            }
        }
        return recordList;
    }

    public IPage<GroupTagDto> getGroupTag(Map<String, Object> params){
        Integer type = MapUtil.getInt(params, "type");
        IPage<GroupTagDto> list = new IPage<>();;
        if (Const.INTEGER_1.equals(type)) {
            IPage<ScanProjectEntity> projectList = scanProjectFeign.page(params);
            if (!CollectionUtils.isEmpty(projectList.getRecords())) {
                List<GroupTagDto> dtoList = new ArrayList<>();
                for (ScanProjectEntity project : projectList.getRecords()) {
                    GroupTagDto dto = GroupTagDto.builder()
                            .tagName("项目").tag(project.getName()).tagValue(project.getId().toString())
                            .build();
                    dtoList.add(dto);
                }
                list.setCurrent(projectList.getCurrent());
                list.setPages(projectList.getPages());
                list.setSize(projectList.getSize());
                list.setTotal(projectList.getTotal());
                list.setRecords(dtoList);
            }
            return list;
        }
        list = scanPortFeign.getGroupTag(params);
        return list;
    }

    public List<KeyValueDto> companyRanking(Map<String, Object> params) {
        List<KeyValueDto> list = scanHostFeign.companyRanking(params);
        return list;
    }

}
