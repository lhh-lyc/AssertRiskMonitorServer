package com.lhh.serveradmin.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.lhh.serveradmin.feign.scan.HostCompanyFeign;
import com.lhh.serveradmin.feign.scan.ScanHostFeign;
import com.lhh.serveradmin.feign.scan.ScanProjectContentFeign;
import com.lhh.serveradmin.feign.scan.ScanningChangeFeign;
import com.lhh.serveradmin.feign.sys.CmsJsonFeign;
import com.lhh.serveradmin.utils.JedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.CmsJsonDto;
import com.lhh.serverbase.dto.FingerprintListDTO;
import com.lhh.serverbase.entity.CmsJsonEntity;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.serverbase.entity.NetErrorDataEntity;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.utils.CopyUtils;
import com.lhh.serverbase.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redisson;
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

    public void checkProject() {
        scanProjectContentFeign.updateEndScanContent();
        /*List<ScanProjectContentEntity> updateList = new ArrayList<>();
        Boolean flag;
        Set<String> projectKeySet = JedisUtils.keysS(String.format(CacheConst.REDIS_SCANNING_PROJECT, '*'));
        List<String> idList = new ArrayList<>();
        projectKeySet.stream().forEach(i -> idList.add(i.substring(i.indexOf(Const.STR_COLON) + 1)));
        List<ScanProjectContentEntity> contentList = scanProjectContentFeign.getContentIpList(idList);
        if (!CollectionUtils.isEmpty(contentList)) {
            for (ScanProjectContentEntity content : contentList) {
                flag = true;
                // 这两个条件不放到sql是因为有的项目只有不扫描的域名，sql查出来更新状态而不是不查出来
                if (!Const.INTEGER_1.equals(content.getIsTop()) && !Const.INTEGER_1.equals(content.getUnknownTop())) {
                    for (String ip : content.getIpList()) {
                        if (JedisUtils.exists(String.format(CacheConst.REDIS_SCANNING_IP, ip))) {
                            flag = false;
                            break;
                        }
                    }
                }
                if (flag) {
                    content.setIsCompleted(Const.INTEGER_1);
                    updateList.add(content);
                }
            }
        }
        if (!CollectionUtils.isEmpty(updateList)) {
            for (ScanProjectContentEntity contentEntity : updateList) {
                scanProjectContentFeign.update(contentEntity);
            }
        }*/
    }

    public void scanningChange() {
        List<NetErrorDataEntity> list = scanningChangeFeign.list(new HashMap<>());
        List<NetErrorDataEntity> hList = list.stream().filter(i -> Const.INTEGER_1.equals(i.getType())).collect(Collectors.toList());
        List<NetErrorDataEntity> ipList = list.stream().filter(i -> Const.INTEGER_2.equals(i.getType())).collect(Collectors.toList());
        List<Long> delIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ipList)) {
            for (NetErrorDataEntity data : ipList) {
                Map<String, Object> params = new HashMap<>();
                params.put("ipLong", data.getObj());
                params.put("scanPorts", data.getScanPorts());
                scanningChangeFeign.endScanIp(params);
                delIds.add(data.getId());
            }
        }
        if (!CollectionUtils.isEmpty(delIds)) {
            scanningChangeFeign.delErrorData(delIds);
            delIds.clear();
        }
        if (!CollectionUtils.isEmpty(hList)) {
            for (NetErrorDataEntity data : hList) {
                Map<String, Object> params = new HashMap<>();
                params.put("domain", data.getObj());
                scanningChangeFeign.endScanDomain(params);
                delIds.add(data.getId());
            }
        }
        if (!CollectionUtils.isEmpty(delIds)) {
            scanningChangeFeign.delErrorData(delIds);
        }
    }

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

        List<CmsJsonEntity> queryList = new ArrayList<>();
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
                queryList.add(entity);
            }
        }
        List<CmsJsonEntity> list = cmsJsonFeign.list(new HashMap<>());
        if (!CollectionUtils.isEmpty(list)) {
            for (CmsJsonEntity json : list) {
                json.setKeywordList(new ArrayList<>(Arrays.asList(json.getKeyword().split(Const.STR_COMMA))));
            }
        }
        queryList.addAll(list);
        List<CmsJsonEntity> domList = queryList.stream().filter(f->"keyword".equals(f.getMethod())).collect(Collectors.toList());
        stringRedisTemplate.opsForValue().set(CacheConst.REDIS_CMS_JSON_LIST, JSON.toJSONString(domList));
        Map<String, String> faviconMap = queryList.stream().filter(f->"faviconhash".equals(f.getMethod())).collect(Collectors.toMap(
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
                    String company = HttpUtils.getDomainUnit(host, false);
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
                    stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_DOMAIN_COMPANY, host), company, 60 * 60 * 24 * 7L, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.error(host + "查询企业失败", e);
                }
            }
        }
        log.info("companyTask执行完毕！");
    }

}
