package com.lhh.servermonitor.service;

import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanHostPortEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.servermonitor.dao.ScanHostPortDao;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.HttpxCustomizeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ScanHostPortService {

    @Value("${dir-setting.tool-dir}")
    private String toolDir;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ScanPortService scanPortService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanHostPortDao scanHostPortDao;
    @Autowired
    ScanProjectHostService scanProjectHostService;

    /**
     * 解析url请求title/cms
     * @param domain
     */
    public void scanSingleHostPortList(String domain) {
        List<Integer> portList = scanPortService.queryDomainPortList(domain);
        List<Integer> ports = scanHostPortDao.queryPortList(domain);
        portList.removeAll(ports);
        if (!CollectionUtils.isEmpty(portList)) {
            List<ScanHostPortEntity> saveList = new ArrayList<>();
            for (Integer port : portList) {
                Map<String, String> result = null;
                try {
                    result = HttpxCustomizeUtils.getUrlMap(stringRedisTemplate, toolDir, domain + Const.STR_COLON + port);
                } catch (IOException e) {
                    log.error("请求错误：" + domain + Const.STR_COLON + port, e);
                    e.printStackTrace();
                }
                if (!CollectionUtils.isEmpty(result)) {
                    ScanHostPortEntity entity = ScanHostPortEntity.builder()
                            .domain(domain).port(port)
                            .url(result.get("url"))
                            .title(result.get("title"))
                            .cms(result.get("cms"))
                            .build();
                    saveList.add(entity);
                }
            }
            if (!CollectionUtils.isEmpty(saveList)) {
                scanHostPortDao.saveBatch(saveList);
            }
        }
    }

}
