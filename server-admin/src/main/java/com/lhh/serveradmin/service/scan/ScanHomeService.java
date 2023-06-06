package com.lhh.serveradmin.service.scan;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serveradmin.feign.scan.*;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.dto.GroupTagDto;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.dto.KeyValueDto;
import com.lhh.serverbase.dto.ScanResultDto;
import com.lhh.serverbase.entity.ScanAddRecordEntity;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.utils.HttpContextUtils;
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
                result = new HashMap<String, Object>(){{put("title", "网站");put("num", numDto.getPortNum());put("type", type);}};
                break;
            case 8:
                numDto = scanPortFeign.getHomeNum(params);
                result = new HashMap<String, Object>(){{put("title", "漏洞");put("num", numDto.getPortNum());put("type", type);}};
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
            case 11:
                contentList = scanProjectContentFeign.list(params);
                List<ScanProjectContentEntity> notCompleteList = contentList.stream().filter(i->Const.INTEGER_0.equals(i.getIsCompleted())).collect(Collectors.toList());
                List<ScanProjectContentEntity> notCompleteDomainList = notCompleteList.stream().filter(i->!RexpUtil.isIP(i.getInputHost())).collect(Collectors.toList());
                result = new HashMap<String, Object>(){{put("title", "主域名收集");put("num", notCompleteDomainList.size());put("type", type);}};
                break;
            case 12:
                contentList = scanProjectContentFeign.list(params);
                notCompleteList = contentList.stream().filter(i->Const.INTEGER_0.equals(i.getIsCompleted())).collect(Collectors.toList());
                List<ScanProjectContentEntity> notCompleteIpList = notCompleteList.stream().filter(i->RexpUtil.isIP(i.getInputHost())).collect(Collectors.toList());
                result = new HashMap<String, Object>(){{put("title", "IP扫描");put("num", notCompleteIpList.size());put("type", type);}};
                break;
        }
        return result;
    }

    public List<ScanAddRecordEntity> getRecordList(Map<String, Object> params) {
        List<ScanAddRecordEntity> recordList = scanAddRecordFeign.list(params);
        String describe = Const.STR_EMPTY;
        if (!CollectionUtils.isEmpty(recordList)) {
            for (ScanAddRecordEntity record : recordList) {
                switch (record.getAddRecordType()) {
                    case 1:
                        describe = record.getParentName() + "  检测出  " + record.getSubName();
                        break;
                    case 2:
                        describe = record.getParentName() + "  新增子域名  " + record.getSubName();
                        break;
                    default:
                        describe = record.getParentName() + "  新增开放端口  " + record.getSubName();
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
