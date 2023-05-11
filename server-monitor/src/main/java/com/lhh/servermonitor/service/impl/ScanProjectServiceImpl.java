package com.lhh.servermonitor.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.dao.ScanProjectDao;
import com.lhh.servermonitor.service.*;
import com.lhh.servermonitor.sync.SyncService;
import com.lhh.servermonitor.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service("scanProjectService")
public class ScanProjectServiceImpl extends ServiceImpl<ScanProjectDao, ScanProjectEntity> implements ScanProjectService {

    @Autowired
    private ScanProjectDao scanProjectDao;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    ScanProjectContentService scanProjectContentService;
    @Autowired
    ExecService execService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    ScanService scanService;
    @Autowired
    SyncService syncService;
    @Autowired
    RedisLock redisLock;

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public IPage<ScanProjectEntity> page(Map<String, Object> params) {
        IPage<ScanProjectEntity> page = this.page(
                new Query<ScanProjectEntity>().getPage(params),
                new QueryWrapper<ScanProjectEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public List<ScanProjectEntity> list(Map<String, Object> params) {
        List<ScanProjectEntity> list = scanProjectDao.queryList(params);
        return list;
    }

    @Override
    public void saveProject(ScanProjectEntity project) {
        // mq分割project，合并缓存问题
//        redisLock.saveProjectRedis(project);
        ScanProjectEntity oldProject = scanProjectDao.selectById(project.getId());
        if (oldProject == null) {
            log.info("项目id=" + project.getId() + "已被删除");
            return;
        }
        if (!CollectionUtils.isEmpty(project.getHostList())) {
            List<String> ipList = project.getHostList().stream().filter(i -> RexpUtil.isIP(i)).collect(Collectors.toList());
            List<String> domainList = project.getHostList().stream().filter(item -> !ipList.contains(item)).collect(Collectors.toList());

            // 已扫描过且端口也扫描一致的域名和ip
            // 过滤掉以前扫描端口不同的数据
            List<ScanProjectHostEntity> projectHostList = new ArrayList<>();
            List<ScanHostEntity> exitHostInfoList = scanHostService.getByDomainList(project.getHostList());
            exitHostInfoList = exitHostInfoList.stream().filter(i -> PortUtils.portEquals(i.getScanPorts(), project.getScanPorts())).collect(Collectors.toList());

            List<String> sameHostList = exitHostInfoList.stream().map(ScanHostEntity::getDomain).collect(Collectors.toList());
            List<String> finalSameHostList = sameHostList.stream().distinct().collect(Collectors.toList());

            // 保存scan_content数据
            List<ScanProjectContentEntity> exitContentList = scanProjectContentService.getExitHostList(project.getId(), project.getHostList());
            Map<String, ScanProjectContentEntity> contentMap = exitContentList.stream().collect(Collectors.toMap(ScanProjectContentEntity::getInputHost, Function.identity(), (key1, key2) -> key2));
            List<ScanProjectContentEntity> updateContentList = new ArrayList<>();
            for (String host : project.getHostList()) {
                if (contentMap.containsKey(host)) {
                    ScanProjectContentEntity content = contentMap.get(host);
                    if (finalSameHostList.contains(host)) {
                        content.setIsCompleted(Const.INTEGER_1);
                        updateContentList.add(content);
                    }
                }
            }
            // todo
            if (!CollectionUtils.isEmpty(updateContentList)) {
//                scanProjectContentService.updateStatus(updateContentList);
                for (ScanProjectContentEntity content : updateContentList) {
                    scanProjectContentService.updateById(content);
                }
            }

            // 子域名关联
            List<ScanProjectHostEntity> saveProjectHostList = new ArrayList<>();
            List<ScanProjectHostEntity> exitProjectHostEntityList = scanProjectHostService.list(new HashMap<String, Object>(){{put("projectId", project.getId());}});
            List<String> exitProjectHostList = exitProjectHostEntityList.stream().map(ScanProjectHostEntity::getHost).collect(Collectors.toList());
            List<ScanHostEntity> exitSubDoMainEntityList = scanHostService.getByParentDomainList(finalSameHostList);
            List<String> exitSubDoMainList = exitSubDoMainEntityList.stream().map(ScanHostEntity::getDomain).collect(Collectors.toList());
            exitSubDoMainList.removeAll(exitProjectHostList);
            if (!CollectionUtils.isEmpty(exitSubDoMainList)) {
                for (String host : exitSubDoMainList) {
                    ScanProjectHostEntity projectHost = ScanProjectHostEntity.builder()
                            .projectId(project.getId()).host(host)
                            .build();
                    saveProjectHostList.add(projectHost);
                }
            }
            scanProjectHostService.saveBatch(saveProjectHostList);

            //扫描新的ip
            Map<String, String> redisMap = new HashMap<>();
            List<String> newIpList = ipList.stream().filter(item -> !finalSameHostList.contains(item)).collect(Collectors.toList());
            List<ScanHostEntity> scanIpList = new ArrayList<>();
            List<ScanParamDto> scanPortParamList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(newIpList)) {
                for (String ip : newIpList) {
                    ScanHostEntity scanHost = ScanHostEntity.builder()
                            .domain(ip).ip(ip).parentDomain(ip)
                            .scanPorts(project.getScanPorts())
                            .type(Const.INTEGER_2).isMajor(Const.INTEGER_0)
                            .build();
                    scanIpList.add(scanHost);

                    // 保存项目-host关联关系
                    ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                            .host(ip).projectId(project.getId())
                            .build();
                    projectHostList.add(item);

                    if (Const.INTEGER_1.equals(project.getPortFlag())) {
                        ScanParamDto dto = ScanParamDto.builder()
                                .subIp(ip)
                                .scanPorts(project.getScanPorts())
                                .build();
                        scanPortParamList.add(dto);
                        Map<String, String> ipMap = new HashMap<>();
                        ipMap.put("ports", project.getScanPorts());
                        ipMap.put("status", Const.STR_0);
                        redisMap.put(String.format(CacheConst.REDIS_SCANNING_IP, ip), JSON.toJSONString(ipMap));
                    }
                }
                scanHostService.saveBatch(scanIpList);
                scanProjectHostService.saveBatch(projectHostList);
            }
            JedisUtils.setPipeJson(redisMap);
            redisMap.clear();
            if (!CollectionUtils.isEmpty(scanPortParamList)) {
//                scanPortInfoService.scanPortList(scanPortParamList);
                syncService.dataHandler(scanPortParamList);
                List<ScanProjectContentEntity> contentList = new ArrayList<>();
                for (ScanParamDto dto : scanPortParamList) {
                    if (!StringUtils.isEmpty(dto.getSubIp())) {
                        contentList = scanProjectContentService.list(new HashMap<String, Object>() {{
                            put("inputHost", dto.getSubIp());
                        }});
                    }
                    if (!CollectionUtils.isEmpty(contentList)) {
                        for (ScanProjectContentEntity content : contentList) {
                            // todo
                            content.setIsCompleted(Const.INTEGER_1);
                            scanProjectContentService.updateById(content);
                        }
                    }
                }
//                if (!CollectionUtils.isEmpty(contentList)) {
//                    scanProjectContentService.updateStatus(contentList);
//                }
            }

            // 扫描新的域名
            List<String> newDomainList = domainList.stream().filter(item -> !finalSameHostList.contains(item)).collect(Collectors.toList());

            List<ScanParamDto> scanDomainParamList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(newDomainList)) {
                for (String host : newDomainList) {
                    ScanParamDto dto = ScanParamDto.builder()
                            .projectId(project.getId())
                            .host(host)
                            .scanPorts(project.getScanPorts())
                            .subDomainFlag(project.getSubDomainFlag())
                            .portFlag(project.getPortFlag())
                            .build();
                    scanDomainParamList.add(dto);
//                    redisMap.put(String.format(CacheConst.REDIS_TASK_DOMAIN, host), Const.STR_1);
                }
            }
            JedisUtils.setPipeJson(redisMap);
            if (!CollectionUtils.isEmpty(scanDomainParamList)) {
                for (ScanParamDto dto : scanDomainParamList) {
                    try {
                        scanService.scanDomainList(dto);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
//        JedisUtils.delKey(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getUserId() + Const.STR_TITLE + project.getName()));
    }

}
