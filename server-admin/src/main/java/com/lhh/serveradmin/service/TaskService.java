package com.lhh.serveradmin.service;

import com.lhh.serveradmin.feign.scan.HostCompanyFeign;
import com.lhh.serveradmin.feign.scan.ScanHostFeign;
import com.lhh.serveradmin.feign.scan.ScanProjectContentFeign;
import com.lhh.serveradmin.feign.scan.ScanningChangeFeign;
import com.lhh.serveradmin.feign.sys.CmsJsonFeign;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.NetErrorDataEntity;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
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

}
