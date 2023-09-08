package com.lhh.serverrefreshdata.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.google.gson.Gson;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.CmsJsonDto;
import com.lhh.serverbase.dto.FingerprintListDTO;
import com.lhh.serverbase.entity.CmsJsonEntity;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.serverbase.utils.HttpUtils;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.serverrefreshdata.feign.scan.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ScanProjectContentFeign scanProjectContentFeign;
    @Autowired
    ScanningChangeFeign scanningChangeFeign;
    @Autowired
    ScanHostFeign scanHostFeign;
    @Autowired
    HostCompanyFeign hostCompanyFeign;
    @Autowired
    CmsJsonFeign cmsJsonFeign;

    public void fingerJson() {
        String response = null;
        try {
            response = HttpUtil.get("https://cdn.jsdelivr.net/gh/EASY233/Finger/library/finger.json");
        } catch (Exception e) {
            log.error("fingerJson请求错误", e);
            return;
        }
        Gson gson = new Gson();
        FingerprintListDTO fingerprintListDTO = gson.fromJson(response, FingerprintListDTO.class);
        List<CmsJsonDto> cmsJsonDtoList = fingerprintListDTO.getFingerprint();
        if (CollectionUtils.isEmpty(cmsJsonDtoList)) {
            log.info("fingerJson查询为空" + response);
            return;
        }

        List<CmsJsonEntity> allList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cmsJsonDtoList)) {
            for (CmsJsonDto dto : cmsJsonDtoList) {
                CmsJsonEntity entity = CmsJsonEntity.builder()
                        .cms(dto.getCms()).method(dto.getMethod())
                        .location(dto.getLocation())
                        .keywordList(dto.getKeyword())
                        .build();
                if (!CollectionUtils.isEmpty(dto.getKeyword())) {
                    entity.setKeyword(String.join(Const.STR_COMMA, dto.getKeyword()));
                }
                allList.add(entity);
            }
        }
        List<CmsJsonEntity> list = cmsJsonFeign.list(new HashMap<>());
        if (!CollectionUtils.isEmpty(list)) {
            for (CmsJsonEntity json : list) {
                json.setKeywordList(new ArrayList<>(Arrays.asList(json.getKeyword().split(Const.STR_COMMA))));
            }
        }
        allList.addAll(list);
        stringRedisTemplate.opsForValue().set(CacheConst.REDIS_CMS_JSON, JSON.toJSONString(allList));
        List<CmsJsonEntity> domList = allList.stream().filter(f->"keyword".equals(f.getMethod())).collect(Collectors.toList());
        stringRedisTemplate.opsForValue().set(CacheConst.REDIS_CMS_JSON_LIST, JSON.toJSONString(domList));
        Map<String, String> faviconMap = allList.stream().filter(f->"faviconhash".equals(f.getMethod())).collect(Collectors.toMap(
                CmsJsonEntity::getKeyword, CmsJsonEntity::getCms, (key1 , key2) -> key1));
        stringRedisTemplate.opsForValue().set(CacheConst.REDIS_CMS_JSON_MAP, JSON.toJSONString(faviconMap));
        log.info("fingerJson更新结束");
    }

    private static List<CmsJsonEntity> mergeObjects(List<CmsJsonEntity> objects) {
        Map<String, CmsJsonEntity> mergedMap = new HashMap<>();
        for (CmsJsonEntity obj : objects) {
            String key = obj.getCms() + obj.getMethod() + obj.getLocation();
            if (mergedMap.containsKey(key)) {
                mergedMap.get(key).addKeywords(obj.getKeywordList());
            } else {
                mergedMap.put(key, obj);
            }
        }
        return new ArrayList<>(mergedMap.values());
    }

    public void companyTask() {
        log.info("companyTask开始执行！");
        List<String> hostList = scanHostFeign.getParentList();
        if (!CollectionUtils.isEmpty(hostList)) {
            for (String host : hostList) {
                try {
                    String company = HttpUtils.getDomainUnit(host);
                    company = StringUtils.isEmpty(company) ? Const.STR_CROSSBAR : company;
                    HostCompanyEntity entity = hostCompanyFeign.queryBasicInfo(host);
                    if (entity == null) {
                        HostCompanyEntity c = HostCompanyEntity.builder()
                                .host(host).company(company)
                                .build();
                        hostCompanyFeign.save(c);
                    } else {
                        if (!company.equals(entity.getCompany())) {
                            entity.setCompany(company);
                            hostCompanyFeign.update(entity);
                        }
                    }
                    String parentDomain = RexpUtil.getMajorDomain(host);
                    String value = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_HOST_INFO, parentDomain));
                    HostCompanyEntity obj;
                    if (StringUtils.isEmpty(value)) {
                        obj = HostCompanyEntity.builder()
                                .host(parentDomain).company(company)
                                .build();
                    } else {
                        obj = JSON.parseObject(value, HostCompanyEntity.class);
                        obj.setCompany(company);
                    }
                    stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_HOST_INFO, parentDomain), JSON.toJSONString(obj));
                } catch (Exception e) {
                    log.error(host + "查询企业失败", e);
                }
            }
        }
        log.info("companyTask执行完毕！");
    }

}
