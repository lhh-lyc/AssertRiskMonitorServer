package com.lhh.serverTask.service;

import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverTask.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service("TaskService")
public class TaskService {

    @Autowired
    ScanHostService scanHostService;

    public void weekTask(){
        List<ScanHostEntity> hostList = scanHostService.getParentDomainList();
        while (!CollectionUtils.isEmpty(hostList)) {
            JedisUtils.setJson(CacheConst.REDIS_TASK_HOST_ID, String.valueOf(hostList.get(hostList.size() - 1).getHostId()));
            for (ScanHostEntity host : hostList) {
                scanHostService.scanHost(host);
//                log.info("id:" + host.getHostId() + "---domain:" + host.getParentDomain());
            }
            hostList = scanHostService.getParentDomainList();
        }
        JedisUtils.delKey(CacheConst.REDIS_TASK_HOST_ID);
        log.info("weekTask更新完毕！");
    }

}
