package com.lhh.serverTask.service;

import com.lhh.serverTask.dao.ScanHostPortDao;
import com.lhh.serverTask.utils.HttpxCustomizeUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanHostPortEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (!CollectionUtils.isEmpty(portList)) {
            List<ScanHostPortEntity> scanList = new ArrayList<>();
            Map<String, ScanHostPortEntity> scanMap = new HashMap<>();
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
                    scanList.add(entity);
                    scanMap.put(port.toString(), entity);
                }
            }
            if (!CollectionUtils.isEmpty(scanList)) {
                List<Integer> scanPortList = scanList.stream().map(ScanHostPortEntity::getPort).collect(Collectors.toList());
                // todo 查询了不是索引的字段，用于更新的判断，可能有隐患
                List<ScanHostPortEntity> exitPortList = scanHostPortDao.queryPortList(domain);
                List<Integer> exitPorts = exitPortList.stream().map(ScanHostPortEntity::getPort).collect(Collectors.toList());
                // 新增部分
                List<ScanHostPortEntity> addList = scanList.stream().filter(p -> !exitPorts.contains(p.getPort())).collect(Collectors.toList());
                // 删除部分
                List<ScanHostPortEntity> delList = exitPortList.stream().filter(p -> !scanPortList.contains(p.getPort())).collect(Collectors.toList());
                // 修改部分
                List<ScanHostPortEntity> upList = exitPortList.stream().filter(p->scanPortList.contains(p.getPort())).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(upList)) {
                    for (ScanHostPortEntity p : upList) {
                        ScanHostPortEntity tmp = scanMap.get(p.getPort().toString());
                        if (tmp != null && (!p.getUrl().equals(tmp.getUrl())
                                || !p.getTitle().equals(tmp.getTitle()) || !p.getCms().equals(tmp.getCms()))) {
                            p.setUrl(scanMap.get(p.getPort().toString()).getUrl());
                            p.setTitle(scanMap.get(p.getPort().toString()).getTitle());
                            p.setCms(scanMap.get(p.getPort().toString()).getCms());
                            scanHostPortDao.updateById(p);
                        }
                    }
                }
                if (!CollectionUtils.isEmpty(addList)) {
                    scanHostPortDao.saveBatch(addList);
                }
                if (!CollectionUtils.isEmpty(delList)) {
                    scanHostPortDao.deleteBatch(delList.stream().map(ScanHostPortEntity::getId).collect(Collectors.toList()));
                }
            }
        }
        stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_END_HOST_PORT, domain), Const.STR_1);
    }

}
