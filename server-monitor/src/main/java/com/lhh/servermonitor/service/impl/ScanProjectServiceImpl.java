package com.lhh.servermonitor.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.*;
import com.lhh.serverbase.utils.DateUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.dao.HostCompanyDao;
import com.lhh.servermonitor.dao.ScanProjectDao;
import com.lhh.servermonitor.mqtt.ExitHoleSender;
import com.lhh.servermonitor.mqtt.IpSender;
import com.lhh.servermonitor.service.*;
import com.lhh.servermonitor.utils.JedisUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("scanProjectService")
public class ScanProjectServiceImpl extends ServiceImpl<ScanProjectDao, ScanProjectEntity> implements ScanProjectService {

    @Autowired
    private ScanProjectDao scanProjectDao;
    @Autowired
    HostCompanyDao hostCompanyDao;
    @Autowired
    HostCompanyService hostCompanyService;
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
    ScanPortService scanPortService;
    @Autowired
    RedisLock redisLock;
    @Autowired
    IpSender mqIpSender;
    @Autowired
    ExitHoleSender mqHoleSender;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    TmpRedisService tmpRedisService;
    @Value("${mqtt-setting.exchange}")
    private String exchange;
    @Value("${mqtt-setting.host-route-key}")
    private String hostRouteKey;

    @Override
    public void sendToMqtt(ScanProjectEntity project) {
        CorrelationData correlationId = new CorrelationData(project.toString());
        //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
        rabbitTemplate.convertAndSend(exchange, hostRouteKey, SerializationUtils.serialize(project), correlationId);
    }

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

    @Override
    public void saveProject(ScanProjectEntity project) {
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
            List<HostCompanyEntity> exitHostInfoList = tmpRedisService.getHostInfoList(project.getHostList());
            Date now = new Date();
            String vailDayStr = stringRedisTemplate.opsForValue().get(CacheConst.REDIS_VAIL_DAY);
            Integer vailDay = StringUtils.isEmpty(vailDayStr) ? Const.INTEGER_0 : Integer.valueOf(vailDayStr);
            exitHostInfoList = exitHostInfoList.stream().filter(i -> PortUtils.portEquals(i.getScanPorts(), project.getScanPorts()) && DateUtils.isInTwoWeek(i.getScanTime(), now, vailDay)).collect(Collectors.toList());
            Map<String, HostCompanyEntity> hostCompanyMap = exitHostInfoList.stream().collect(Collectors.toMap(HostCompanyEntity::getHost, h -> h));
            List<String> sameHostList = exitHostInfoList.stream().map(HostCompanyEntity::getHost).collect(Collectors.toList());
            List<String> finalScanHostList = sameHostList.stream().distinct().collect(Collectors.toList());

            // 子域名关联
            // 按已存在且不重复扫描的主域名，查询已存在的子域名列表
            List<ScanProjectHostEntity> saveProjectHostList = new ArrayList<>();
            List<ScanHostEntity> exitSubDoMainEntityList = scanHostService.getByParentDomainList(finalScanHostList);
            List<String> exitSubDoMainList = exitSubDoMainEntityList.stream().map(ScanHostEntity::getDomain).distinct().collect(Collectors.toList());
            List<ScanParamDto> sendHoleList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(exitSubDoMainList)) {
                for (String subDomain : exitSubDoMainList) {
                    String domain = RexpUtil.getMajorDomain(subDomain);
                    ScanProjectHostEntity projectHost = ScanProjectHostEntity.builder()
                            .projectId(project.getId()).host(subDomain)
                            .parentDomain(domain).isScanning(Const.INTEGER_0)
                            .build();
                    saveProjectHostList.add(projectHost);

                    if (Const.INTEGER_1.equals(project.getNucleiFlag()) || Const.INTEGER_1.equals(project.getAfrogFlag()) || Const.INTEGER_1.equals(project.getXrayFlag())) {
                        List<Integer> portList = scanPortService.queryPortList(subDomain);
                        String ports = RexpUtil.isIP(subDomain) ? tmpRedisService.getHostInfo(subDomain).getScanPorts() : tmpRedisService.getHostInfo(domain).getScanPorts();
                        if (!CollectionUtils.isEmpty(portList)) {
                            for (Integer port : portList) {
                                ScanParamDto dto = ScanParamDto.builder()
                                        .projectId(project.getId()).domain(domain)
                                        .subDomain(subDomain).scanPorts(ports)
                                        .port(port)
                                        .build();
                                // 已扫描的域名或ip重新扫描漏洞
                                sendHoleList.add(dto);
                            }
                        }
                    }
                }
            }
            mqHoleSender.sendExitHoleToMqtt(sendHoleList);
            scanProjectHostService.saveBatch(saveProjectHostList);
            // 不扫漏洞，已存在的域名流程直接结束
            if (!Const.INTEGER_1.equals(project.getNucleiFlag()) && !Const.INTEGER_1.equals(project.getAfrogFlag()) && !Const.INTEGER_1.equals(project.getXrayFlag())) {
                if (!CollectionUtils.isEmpty(finalScanHostList)) {
                    for (String h : finalScanHostList) {
                        redisLock.removeProjectRedis(project.getId(), h);
                    }
                }
            }

            //扫描新的ip
            Map<String, String> redisMap = new HashMap<>();
            List<String> newIpList = ipList.stream().filter(item -> !finalScanHostList.contains(item)).collect(Collectors.toList());
            List<ScanParamDto> scanPortParamList = new ArrayList<>();
            String projectStr = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()));
            ScanProjectEntity redisProject = JSON.parseObject(projectStr, ScanProjectEntity.class);
            if (!CollectionUtils.isEmpty(newIpList)) {
                for (String ip : newIpList) {
                    String ports = tmpRedisService.getHostInfo(ip).getScanPorts();
                    String allPorts = PortUtils.getAllPorts(ports, project.getScanPorts());
                    // 保存项目-host关联关系
                    ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                            .projectId(project.getId()).isScanning(Const.INTEGER_0)
                            .host(ip).parentDomain(ip)
                            .build();
                    if (Const.INTEGER_1.equals(project.getPortFlag())) {
                        item.setIsScanning(Const.INTEGER_1);
                        ScanParamDto dto = ScanParamDto.builder()
                                .projectId(project.getId())
                                .subIp(ip)
                                .scanPorts(project.getScanPorts())
                                .allPorts(allPorts)
                                .scanTime(hostCompanyMap.get(ip) != null ? hostCompanyMap.get(ip).getScanTime() : null)
                                .portTool(redisProject == null ? Const.INTEGER_1 : redisProject.getPortTool())
                                .build();
                        scanPortParamList.add(dto);
                    }
                    projectHostList.add(item);
                }
                scanProjectHostService.saveBatch(projectHostList);
            }
            JedisUtils.setPipeJson(redisMap);
            if (!CollectionUtils.isEmpty(scanPortParamList)) {
                mqIpSender.sendScanningIpToMqtt(scanPortParamList);
            }

            // 扫描新的域名
            List<String> newDomainList = domainList.stream().filter(item -> !finalScanHostList.contains(item)).collect(Collectors.toList());

            List<ScanParamDto> scanDomainParamList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(newDomainList)) {
                for (String host : newDomainList) {
                    ScanParamDto dto = ScanParamDto.builder()
                            .projectId(project.getId())
                            .host(host)
                            .scanPorts(project.getScanPorts())
                            .scanTime(hostCompanyMap.get(host) != null ? hostCompanyMap.get(host).getScanTime() : null)
                            .subDomainFlag(project.getSubDomainFlag())
                            .portFlag(project.getPortFlag())
                            .build();
                    scanDomainParamList.add(dto);
                }
                for (ScanParamDto dto : scanDomainParamList) {
                    scanService.scanDomainList(dto);
                }
            } else {
                // 未关联到子域名也不存在于host表，说明不是主域名(非法域名输入)增加一条关联关系（主域名不需要，因为上面查询子域名的时候关联了）
                // 不需要重新扫描的域名，在此维护scan_project_host表关联
                if (CollectionUtils.isEmpty(saveProjectHostList) && !CollectionUtils.isEmpty(exitSubDoMainList)) {
                    List<ScanProjectHostEntity> projectDomainList = new ArrayList<>();
                    for (String host : finalScanHostList) {
                        ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                                .projectId(project.getId())
                                .parentDomain(RexpUtil.getMajorDomain(host)).host(host)
                                .isScanning(Const.INTEGER_0)
                                .build();
                        projectDomainList.add(item);
                    }
                    scanProjectHostService.saveBatch(projectDomainList);
                }
            }
        }
    }

    @Override
    public List<ScanProjectEntity> getByNameAndUserId(Long userId, String name) {
        return scanProjectDao.getByNameAndUserId(userId, name);
    }

}
