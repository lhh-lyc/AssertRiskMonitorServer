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
        }
    }
}
