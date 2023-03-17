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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public void saveProject(ScanProjectEntity project) {
        if (!CollectionUtils.isEmpty(project.getHostList())) {
            List<String> ipList = project.getHostList().stream().filter(i -> RexpUtil.isIP(i)).collect(Collectors.toList());
            List<String> domainList = project.getHostList().stream().filter(item -> !ipList.contains(item)).collect(Collectors.toList());

            // 已扫描过的域名和ip
            List<ScanProjectHostEntity> projectHostList = new ArrayList<>();
            // 过滤redis扫描过且端口也扫描一致的域名和ip
            List<String> sameIpList = new ArrayList<>();
            List<String> exitIpList = ipList.stream().filter(i -> JedisUtils.exists(String.format(CacheConst.REDIS_IP_INFO, i)))
                    .map(i -> String.format(CacheConst.REDIS_IP_INFO, i)).collect(Collectors.toList());
            Map<String, String> exitIpInfoList = JedisUtils.getPipeJson(exitIpList);
            if (!CollectionUtils.isEmpty(exitIpInfoList)) {
                for (String key : exitIpInfoList.keySet()) {
                    JSONObject obj = JSON.parseObject(exitIpInfoList.get(key));
                    if (PortUtils.portEquals(MapUtil.getStr(obj, "ports"), project.getPorts())) {
                        sameIpList.add(MapUtil.getStr(obj, "host"));
                    }
                }
            }
            List<String> sameDomainList = new ArrayList<>();
            List<String> exitDomainList = domainList.stream().filter(i -> JedisUtils.exists(String.format(CacheConst.REDIS_IP_INFO, i)))
                    .map(i -> String.format(CacheConst.REDIS_IP_INFO, i)).collect(Collectors.toList());
            Map<String, String> exitDomainInfoList = JedisUtils.getPipeJson(exitDomainList);
            if (!CollectionUtils.isEmpty(exitDomainInfoList)) {
                for (String key : exitDomainInfoList.keySet()) {
                    JSONObject obj = JSON.parseObject(exitDomainInfoList.get(key));
                    if (PortUtils.portEquals(MapUtil.getStr(obj, "ports"), project.getPorts())) {
                        sameDomainList.add(MapUtil.getStr(obj, "host"));
                    }
                }
            }

            sameIpList.addAll(sameDomainList);
            Map<String, String> exitValueMap = JedisUtils.getPipeJson(sameIpList);
            if (!CollectionUtils.isEmpty(exitValueMap)) {
                for (String key : exitValueMap.keySet()) {
                    Map<String, String> hostMap = JSON.parseObject(exitValueMap.get(key), Map.class);
                    if (!CollectionUtils.isEmpty(hostMap)) {
                        ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                                .projectId(project.getId()).host(String.valueOf(hostMap.get("host")))
                                .build();
                        projectHostList.add(item);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(projectHostList)) {
                scanProjectHostService.saveBatch(projectHostList);
                projectHostList.clear();
            }

            //扫描新的ip
            List<String> newIpList = ipList.stream().filter(item -> !sameIpList.contains(item)).collect(Collectors.toList());
            List<String> ipKeyList = ipList.stream().map(i -> String.format(CacheConst.REDIS_IP_INFO, i)).collect(Collectors.toList());
            Map<String, String> ipInfoMap = JedisUtils.getPipeJson(ipKeyList);
            List<ScanHostEntity> scanIpList = new ArrayList<>();
            List<ScanParamDto> scanPortParamList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(newIpList)) {
                for (String host : newIpList) {
                    ScanHostEntity scanHost = ScanHostEntity.builder()
                            .host(host)
                            .parentHost(Const.STR_0).type(Const.INTEGER_2)
                            .build();
                    scanIpList.add(scanHost);
                }
                scanHostService.saveBatch(scanIpList);

                // 保存项目-host关联关系
                for (ScanHostEntity scanHost : scanIpList) {
                    String oldPorts = Const.STR_EMPTY;
                    String info = ipInfoMap.get(String.format(CacheConst.REDIS_IP_INFO, scanHost.getHost()));
                    if (!StringUtils.isEmpty(info)) {
                        oldPorts = MapUtil.getStr(JSONObject.parseObject(info), "ports");
                    }
                    ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                            .host(scanHost.getHost()).projectId(project.getId())
                            .ports(PortUtils.getNewPorts(oldPorts, project.getPorts()))
                            .build();
                    projectHostList.add(item);

                    ScanParamDto dto = ScanParamDto.builder()
                            .projectId(project.getId())
                            .host(scanHost.getHost())
                            .ports(project.getPorts())
                            .build();
                    scanPortParamList.add(dto);
                }
                if (!CollectionUtils.isEmpty(projectHostList)) {
                    scanProjectHostService.saveBatch(projectHostList);
                    projectHostList.clear();
                }
            }
            if (!CollectionUtils.isEmpty(scanPortParamList)) {
                for (ScanParamDto dto : scanPortParamList) {
                    scanPortInfoService.scanPortList(dto);
                }
            }

            // 扫描新的域名
            List<String> newDomainList = domainList.stream().filter(item -> !sameDomainList.contains(item)).collect(Collectors.toList());
            List<String> domainKeyList = ipList.stream().map(i -> String.format(CacheConst.REDIS_IP_INFO, i)).collect(Collectors.toList());
            Map<String, String> domainInfoMap = JedisUtils.getPipeJson(domainKeyList);
            List<ScanParamDto> scanDomainParamList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(newDomainList)) {
                for (String host : newDomainList) {
                    // 保存项目-host关联关系
                    String oldPorts = Const.STR_EMPTY;
                    String info = domainInfoMap.get(String.format(CacheConst.REDIS_IP_INFO, host));
                    if (!StringUtils.isEmpty(info)) {
                        oldPorts = MapUtil.getStr(JSONObject.parseObject(info), "ports");
                    }
                    ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                            .host(host).projectId(project.getId())
                            .ports(PortUtils.getNewPorts(oldPorts, project.getPorts()))
                            .build();
                    projectHostList.add(item);

                    ScanParamDto dto = ScanParamDto.builder()
                            .projectId(project.getId())
                            .host(host)
                            .ports(project.getPorts())
                            .build();
                    scanDomainParamList.add(dto);
                }
                if (!CollectionUtils.isEmpty(projectHostList)) {
                    scanProjectHostService.saveBatch(projectHostList);
                }
            }
            if (!CollectionUtils.isEmpty(scanDomainParamList)) {
                for (ScanParamDto dto : scanDomainParamList) {
                    scanService.scanDomainList(dto);
                }
            }
        }
    }

}
