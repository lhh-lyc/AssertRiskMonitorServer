package com.lhh.serveradmin.service;

import com.lhh.serveradmin.feign.scan.ScanProjectContentFeign;
import com.lhh.serveradmin.utils.JedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class TaskService {

    @Autowired
    ScanProjectContentFeign scanProjectContentFeign;

    public void checkProject() {
        List<ScanProjectContentEntity> updateList = new ArrayList<>();
        Boolean flag;
        Set<String> projectKeySet = JedisUtils.keysS(String.format(CacheConst.REDIS_SCANNING_PROJECT, '*'));
        List<String> idList = new ArrayList<>();
        projectKeySet.stream().forEach(i -> idList.add(i.replace(CacheConst.REDIS_SCANNING_PROJECT, Const.STR_EMPTY)));
        List<ScanProjectContentEntity> contentList = scanProjectContentFeign.getContentIpList(idList);
        if (!CollectionUtils.isEmpty(contentList)) {
            for (ScanProjectContentEntity content : contentList) {
                flag = true;
                for (String ip : content.getIpList()) {
                    if (JedisUtils.exists(String.format(CacheConst.REDIS_SCANNING_IP, ip))) {
                        flag = false;
                        break;
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
        }
    }
}
