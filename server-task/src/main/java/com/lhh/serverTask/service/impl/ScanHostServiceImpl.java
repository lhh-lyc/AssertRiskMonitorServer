package com.lhh.serverTask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverTask.utils.ExecUtil;
import com.lhh.serverTask.utils.PortUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.CopyUtils;
import com.lhh.serverbase.utils.HttpUtils;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverTask.dao.ScanHostDao;
import com.lhh.serverTask.service.ScanHostService;
import com.lhh.serverTask.utils.JedisUtils;
import com.lhh.serverbase.utils.RexpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service("scanHostService")
public class ScanHostServiceImpl extends ServiceImpl<ScanHostDao, ScanHostEntity> implements ScanHostService {

    @Value("${dir-setting.subfinder-dir}")
    private String subfinderDir;

    @Autowired
    private ScanHostDao scanHostDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanHostEntity> page(Map<String, Object> params) {
        IPage<ScanHostEntity> page = this.page(
                new Query<ScanHostEntity>().getPage(params),
                new QueryWrapper<ScanHostEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanHostEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanHostEntity> getParentDomainList() {
        Long hostId = JedisUtils.exists(CacheConst.REDIS_TASK_HOST_ID) ? Long.valueOf(JedisUtils.getStr(CacheConst.REDIS_TASK_HOST_ID)) : Const.LONG_0;
        QueryWrapper wrapper = Wrappers.query().select("host_id, parent_domain, scan_ports")
                .eq("del_flg", Const.INTEGER_0)
                .gt("host_id", hostId)
                .groupBy("parent_domain")
                .orderByAsc("host_id")
                .last(" limit " + Const.INTEGER_100);
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanHostEntity> getByDomainList(List<String> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return new ArrayList<>();
        }
        QueryWrapper wrapper = Wrappers.query()
                .in("domain", hostList);
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanHostEntity> getByIpList(List<String> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return new ArrayList<>();
        }
        QueryWrapper wrapper = Wrappers.query()
                .in("ip", hostList);
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public void scanHost(ScanHostEntity hostEntity) {
        if (!RexpUtil.isIP(hostEntity.getParentDomain())) {
            log.info(hostEntity.getParentDomain() + "子域名收集");
            // 子域名列表
            String cmd = String.format(Const.STR_SUBFINDER_SUBDOMAIN, subfinderDir, hostEntity.getParentDomain());
            SshResponse response = null;
            try {
                response = ExecUtil.runCommand(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<String> subdomainList = response.getOutList();
            List<ScanParamDto> dtoList = new ArrayList<>();
            log.info(CollectionUtils.isEmpty(subdomainList) ? hostEntity.getParentDomain() + "未扫描到子域名" : hostEntity.getParentDomain() + "子域名有:" + String.join(Const.STR_COMMA, subdomainList));
            subdomainList.add(hostEntity.getParentDomain());

        } else {

        }
    }

    public void scanDomainList(ScanParamDto scanDto) {
        log.info(scanDto.getHost() + "子域名收集");
        // 子域名列表
        String cmd = String.format(Const.STR_SUBFINDER_SUBDOMAIN, subfinderDir, scanDto.getHost());
        SshResponse response = null;
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> subdomainList = response.getOutList();
        List<ScanParamDto> dtoList = new ArrayList<>();
        log.info(CollectionUtils.isEmpty(subdomainList) ? scanDto.getHost() + "未扫描到子域名" : scanDto.getHost() + "子域名有:" + String.join(Const.STR_COMMA, subdomainList));
        subdomainList.add(scanDto.getHost());
        List<ScanHostEntity> saveHostList = new ArrayList<>();
        List<ScanHostEntity> updateHostList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(subdomainList)) {
            List<ScanHostEntity> exitHostInfoList = scanHostService.getByDomainList(subdomainList);
            Map<String, ScanHostEntity> hostMap = exitHostInfoList.stream().collect(Collectors.toMap(ScanHostEntity::getDomain, Function.identity(), (key1, key2) -> key2));
            for (String subdomain : subdomainList) {
                ScanHostEntity host = hostMap.get(subdomain);
                if (host != null) {
                    if (PortUtils.portEquals(host.getScanPorts(), scanDto.getScanPorts())) {
                        // 已扫描域名跳过
                        // 子域名循环，当子域名不等于输入域名域名且mysql父域名等于该域名时，说明是子域名已存在mysql并该更新父域名了
                        if (!scanDto.getHost().equals(subdomain) && host.getParentDomain().equals(subdomain)) {
                            host.setParentDomain(scanDto.getHost());
                            updateHostList.add(host);
                        }
                        continue;
                    } else {
                        // 域名扫描过但端口不一样
                        host.setScanPorts(PortUtils.getNewPorts(host.getScanPorts(), scanDto.getScanPorts()));
                        updateHostList.add(host);
                    }
                }
                ScanParamDto dto = new ScanParamDto();
                CopyUtils.copyProperties(scanDto, dto);
                dto.setSubDomain(subdomain);
                dtoList.add(dto);
            }

            // 子域名解析ip
            Map<String, String> redisMap = new HashMap<>();
            List<ScanProjectHostEntity> projectHostList = new ArrayList<>();
            List<ScanParamDto> ipInfoList = getDomainIpList(dtoList);
            List<ScanParamDto> scanPortParamList = new ArrayList<>();
            List<String> ipList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(ipInfoList)) {
                for (ScanParamDto dto : ipInfoList) {
                    if (!CollectionUtils.isEmpty(dto.getSubIpList())) {
                        for (String ip : dto.getSubIpList()) {
                            ipList.add(ip);
                            redisMap.put(String.format(CacheConst.REDIS_SCANNING_IP, ip), dto.getScanPorts());
                        }
                    }
                }
                List<ScanHostEntity> exitIpInfoList = scanHostService.getByIpList(ipList);
                Map<String, ScanHostEntity> ipMap = exitIpInfoList.stream().collect(Collectors.toMap(ScanHostEntity::getIp, Function.identity(), (key1, key2) -> key2));
                String ports = ipInfoList.get(0).getScanPorts();
                String company = HttpUtils.getDomainUnit(scanDto.getHost());
                for (ScanParamDto sub : ipInfoList) {
                    if (CollectionUtils.isEmpty(sub.getSubIpList())) {
                        break;
                    }
                    for (String ip : sub.getSubIpList()) {
                        ScanParamDto dto = ScanParamDto.builder()
                                .subIp(ip).scanPorts(ports)
                                .build();
                        scanPortParamList.add(dto);

                        ScanHostEntity exitIp = ipMap.get(ip);
                        // 新的域名与ip组合
                        if (exitIp == null || (exitIp != null && !exitIp.getDomain().equals(sub.getDomain()))) {
                            ScanHostEntity host = ScanHostEntity.builder()
                                    .parentDomain(scanDto.getHost())
                                    .domain(sub.getSubDomain())
                                    .ip(ip).scanPorts(scanDto.getScanPorts())
                                    .company(company)
                                    .type(Const.INTEGER_3)
                                    .subIpList(sub.getSubIpList())
                                    .build();
                            saveHostList.add(host);
                        }
                        // 更新域名扫描端口
                        if (exitIp != null && !PortUtils.portEquals(exitIp.getScanPorts(), scanDto.getScanPorts())) {
                            exitIp.setScanPorts(PortUtils.getNewPorts(exitIp.getScanPorts(), scanDto.getScanPorts()));
                            updateHostList.add(exitIp);
                        }
                    }

                    // 保存项目-host关联关系
                    ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                            .projectId(scanDto.getProjectId()).host(sub.getSubDomain())
                            .build();
                    projectHostList.add(item);
                }
            }

            if (!CollectionUtils.isEmpty(saveHostList)) {
                scanHostService.saveBatch(saveHostList);
            }
            if (!CollectionUtils.isEmpty(updateHostList)) {
                // todo
                for (ScanHostEntity host : updateHostList) {
                    scanHostService.updateById(host);
                }
            }
            if (!CollectionUtils.isEmpty(projectHostList)) {
                scanProjectHostService.saveBatch(projectHostList);
            }
            if (!CollectionUtils.isEmpty(redisMap)) {
                JedisUtils.setPipeJson(redisMap);
            }
//            JedisUtils.delKey(String.format(CacheConst.REDIS_TASK_DOMAIN, scanDto.getHost()));
            if (!CollectionUtils.isEmpty(scanPortParamList)) {
                scanPortParamList = scanPortParamList.stream().distinct().collect(Collectors.toList());
                scanPortInfoService.scanPortList(scanPortParamList);
            }
        }
    }

    /**
     * java代码解析子域名ip
     */
    public List<ScanParamDto> getDomainIpList(List<ScanParamDto> dtoList) {
        List<ScanParamDto> ipList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dtoList)) {
            for (ScanParamDto dto : dtoList) {
                try {
                    List<String> list = new ArrayList<>();
                    InetAddress[] inetadd = InetAddress.getAllByName(dto.getSubDomain());
                    //遍历所有的ip并输出
                    for (int i = 0; i < inetadd.length; i++) {
                        if (!StringUtils.isEmpty(inetadd[i] + Const.STR_EMPTY)) {
                            String ip = (inetadd[i] + Const.STR_EMPTY).split(Const.STR_SLASH)[1];
                            if (RexpUtil.isIP(ip)) {
                                list.add(ip);
                            }
                        }
                    }
                    if (CollectionUtils.isEmpty(list)) {
                        log.info(dto.getSubDomain() + "未解析出ip");
                        break;
                    }
                    String ips = String.join(Const.STR_COMMA, list);
                    log.info(dto.getSubDomain() + "解析ip为：" + ips);
                    dto.setSubIpList(list);
                    ipList.add(dto);
                } catch (UnknownHostException e) {
                    log.error(dto.getSubDomain() + "解析ip出错");
                }
            }
        }
        return ipList;
    }

}
