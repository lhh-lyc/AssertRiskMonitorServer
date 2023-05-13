package com.lhh.serveradmin.service.scan;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serveradmin.feign.scan.*;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.dto.GroupTagDto;
import com.lhh.serverbase.dto.HomeNumDto;
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

    public List<Map<String, Object>> getHomeNum(Map<String, Object> params) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<ScanProjectEntity> projectList = scanProjectFeign.list(params);
        Integer projectNum = projectList.size();
        List<ScanResultDto> list = scanHostFeign.getDomainGroupList(params);
        HomeNumDto numDto = scanPortFeign.getHomeNum(params);
        Integer companyNum = list.stream().filter(c->!StringUtils.isEmpty(c.getCompany())).collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ScanResultDto :: getCompany))), ArrayList::new)).size();
        Integer ipNum = scanPortFeign.getGroupTagNum(new HashMap<String, Object>(){{put("type", 5);}});
        List<String> primaryDomainList = list.stream().filter(c->Const.INTEGER_1.equals(c.getIsDomain())).map(ScanResultDto::getParentDomain).distinct().collect(Collectors.toList());
        Integer primaryDomainNum = primaryDomainList.size();
        List<ScanResultDto> subDomainList = list.stream().filter(c->Const.INTEGER_0.equals(c.getIsMajor())&&Const.INTEGER_1.equals(c.getIsDomain())).collect(Collectors.toList());
        Integer subDomainNum = subDomainList.size();
        List<ScanProjectContentEntity> contentList = scanProjectContentFeign.list(params);
        List<ScanProjectContentEntity> completeList = contentList.stream().filter(i->Const.INTEGER_1.equals(i.getIsCompleted())).collect(Collectors.toList());
        List<ScanProjectContentEntity> notCompleteList = contentList.stream().filter(i->Const.INTEGER_0.equals(i.getIsCompleted())).collect(Collectors.toList());
        List<ScanProjectContentEntity> completeIpList = completeList.stream().filter(i->RexpUtil.isIP(i.getInputHost())).collect(Collectors.toList());
        List<ScanProjectContentEntity> completeDomainList = completeList.stream().filter(i->!RexpUtil.isIP(i.getInputHost())).collect(Collectors.toList());
        List<ScanProjectContentEntity> notCompleteIpList = notCompleteList.stream().filter(i->RexpUtil.isIP(i.getInputHost())).collect(Collectors.toList());
        List<ScanProjectContentEntity> notCompleteDomainList = notCompleteList.stream().filter(i->!RexpUtil.isIP(i.getInputHost())).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<String, Object>(){{put("title", "项目");put("num", projectNum);put("type", 1);}};
        resultList.add(result);
        result = new HashMap<String, Object>(){{put("title", "企业");put("num", companyNum);put("type", 2);}};
        resultList.add(result);
        Integer finalPrimaryDomainNum = primaryDomainNum;
        result = new HashMap<String, Object>(){{put("title", "主域名");put("num", finalPrimaryDomainNum);put("type", 3);}};
        resultList.add(result);
        Integer finalSubDomainNum = subDomainNum;
        result = new HashMap<String, Object>(){{put("title", "子域名");put("num", finalSubDomainNum);put("type", 4);}};
        resultList.add(result);
        result = new HashMap<String, Object>(){{put("title", "IP");put("num", ipNum);put("type", 5);}};
        resultList.add(result);
        result = new HashMap<String, Object>(){{put("title", "端口");put("num", numDto.getPortNum());put("type", 6);}};
        resultList.add(result);
        result = new HashMap<String, Object>(){{put("title", "网站");put("num", numDto.getPortNum());put("type", 7);}};
        resultList.add(result);
        result = new HashMap<String, Object>(){{put("title", "漏洞");put("num", numDto.getPortNum());put("type", 8);}};
        resultList.add(result);
        result = new HashMap<String, Object>(){{put("title", "主域名已收集");put("num", completeDomainList.size());put("type", 9);}};
        resultList.add(result);
        result = new HashMap<String, Object>(){{put("title", "IP已扫描");put("num", completeIpList.size());put("type", 10);}};
        resultList.add(result);
        result = new HashMap<String, Object>(){{put("title", "主域名收集");put("num", notCompleteDomainList.size());put("type", 11);}};
        resultList.add(result);
        result = new HashMap<String, Object>(){{put("title", "IP扫描");put("num", notCompleteIpList.size());put("type", 12);}};
        resultList.add(result);
        return resultList;
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

}
