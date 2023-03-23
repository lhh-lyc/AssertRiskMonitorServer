package com.lhh.servermonitor.service.impl;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.CacheConst;
import com.lhh.serverbase.utils.Const;
import com.lhh.serverbase.utils.Query;
import com.lhh.servermonitor.dao.ScanProjectDao;
import com.lhh.servermonitor.dto.ScanParamDto;
import com.lhh.servermonitor.service.*;
import com.lhh.servermonitor.utils.JedisUtils;
import com.lhh.servermonitor.utils.PortUtils;
import com.lhh.servermonitor.utils.RexpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service("scanProjectService")
public class ScanProjectServiceImpl extends ServiceImpl<ScanProjectDao, ScanProjectEntity> implements ScanProjectService {

    @Autowired
    private ScanProjectDao scanProjectDao;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    ExecService execService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    ScanService scanService;

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
        QueryWrapper wrapper = Wrappers.query();
        List<ScanProjectEntity> list = list(wrapper);
        return list;
    }

    @Async
    @Override
    public void saveProject(ScanProjectEntity project) {
        JedisUtils.setJson(String.format(CacheConst.REDIS_TASK_PROJECT, project.getUserId() + Const.STR_TITLE + project.getName()), JSON.toJSONString(project));
        if (!CollectionUtils.isEmpty(project.getHostList())) {
            List<String> ipList = project.getHostList().stream().filter(i -> RexpUtil.isIP(i)).collect(Collectors.toList());
            List<String> domainList = project.getHostList().stream().filter(item -> !ipList.contains(item)).collect(Collectors.toList());

            // 已扫描过且端口也扫描一致的域名和ip
            List<ScanProjectHostEntity> projectHostList = new ArrayList<>();
            List<ScanHostEntity> exitHostInfoList = scanHostService.getByDomainList(project.getHostList());
            if (!CollectionUtils.isEmpty(exitHostInfoList)) {
                for (ScanHostEntity host : exitHostInfoList) {
                    ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                            .projectId(project.getId()).host(host.getDomain())
                            .build();
                    projectHostList.add(item);
                }
            }

            if (!CollectionUtils.isEmpty(projectHostList)) {
                scanProjectHostService.saveBatch(projectHostList);
                projectHostList.clear();
            }

            // 过滤掉以前扫描端口不同的数据
            exitHostInfoList = exitHostInfoList.stream().filter(i -> PortUtils.portEquals(i.getScanPorts(), project.getScanPorts())).collect(Collectors.toList());
            List<String> sameHostList = exitHostInfoList.stream().map(ScanHostEntity::getDomain).collect(Collectors.toList());

            //扫描新的ip
            Map<String, String> redisMap = new HashMap<>();
            List<String> newIpList = ipList.stream().filter(item -> !sameHostList.contains(item)).collect(Collectors.toList());
            List<ScanHostEntity> scanIpList = new ArrayList<>();
            List<ScanParamDto> scanPortParamList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(newIpList)) {
                for (String ip : newIpList) {
                    ScanHostEntity scanHost = ScanHostEntity.builder()
                            .domain(ip).ip(ip).parentDomain(ip)
                            .scanPorts(project.getScanPorts()).type(Const.INTEGER_2)
                            .build();
                    scanIpList.add(scanHost);

                    // 保存项目-host关联关系
                    ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                            .host(ip).projectId(project.getId())
                            .build();
                    projectHostList.add(item);

                    ScanParamDto dto = ScanParamDto.builder()
                            .subIp(ip)
                            .scanPorts(project.getScanPorts())
                            .build();
                    scanPortParamList.add(dto);
                    redisMap.put(String.format(CacheConst.REDIS_TASK_IP, ip), project.getScanPorts());
                }
                scanHostService.saveBatch(scanIpList);
                scanProjectHostService.saveBatch(projectHostList);
            }
            JedisUtils.setPipeJson(redisMap);
            redisMap.clear();
            if (!CollectionUtils.isEmpty(scanPortParamList)) {
                scanPortInfoService.scanPortList(scanPortParamList);
            }

            // 扫描新的域名
            List<String> newDomainList = domainList.stream().filter(item -> !sameHostList.contains(item)).collect(Collectors.toList());
            List<ScanParamDto> scanDomainParamList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(newDomainList)) {
                for (String host : newDomainList) {
                    ScanParamDto dto = ScanParamDto.builder()
                            .projectId(project.getId())
                            .host(host)
                            .scanPorts(project.getScanPorts())
                            .build();
                    scanDomainParamList.add(dto);
//                    redisMap.put(String.format(CacheConst.REDIS_TASK_DOMAIN, host), Const.STR_1);
                }
            }
            JedisUtils.setPipeJson(redisMap);
            if (!CollectionUtils.isEmpty(scanDomainParamList)) {
                for (ScanParamDto dto : scanDomainParamList) {
                    scanService.scanDomainList(dto);
                }
            }
        }
        JedisUtils.delKey(String.format(CacheConst.REDIS_TASK_PROJECT, project.getUserId() + Const.STR_TITLE + project.getName()));
    }

}
