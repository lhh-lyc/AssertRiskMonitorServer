package com.lhh.servermonitor.mqtt;

import com.alibaba.fastjson2.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.DateUtils;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.dao.ScanProjectDao;
import com.lhh.servermonitor.service.*;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(value = "scanningHostData", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ScanningHostListener {

    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanPortService scanPortService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    ScanHostPortService scanHostPortService;
    @Autowired
    ScanHoleService scanHoleService;
    @Autowired
    ScanProjectContentService scanProjectContentService;
    @Autowired
    HostCompanyService hostCompanyService;
    @Autowired
    private ScanProjectDao scanProjectDao;
    @Autowired
    IpSender mqIpSender;
    @Autowired
    HoleSender holeSender;
    @Autowired
    RedissonClient redisson;
    @Autowired
    RedisLock redisLock;
    @Autowired
    TmpRedisService tmpRedisService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ScanParamDto dto = (ScanParamDto) SerializationUtils.deserialize(bytes);
        // todo bug:好像没做端口不同扫描的处理，不同的端口直接跳过去了
        //  解决办法：1.content表保留scan_ports字段
        //  2.host_company表存一个预先的端口字段，新建任务就更新，存一个已完成的端口字段，每个子域名扫描完再更新，
        //  预先端口用于刚开始过滤已扫描主域名，已完成端口用于中间过程端口处理
//        String lockKey = String.format(CacheConst.REDIS_LOCK_SUBDOMAIN, dto.getSubDomain());
//        RLock lock = redisson.getLock(lockKey);
//        boolean success = true;
//        try {
//            success = lock.tryLock(1, TimeUnit.SECONDS);
//            if (success) {
        deal(dto, message, channel);
//            }
//        } catch (Exception e) {} finally {
//            // 判断当前线程是否持有锁
//            if (success && lock.isHeldByCurrentThread()) {
//                //释放当前锁
//                lock.unlock();
//            }
//        }
    }

    public void deal(ScanParamDto dto, Message message, Channel channel) {
        try {
            log.info("开始处理项目" + dto.getProjectId() + "域名：" + dto.getSubDomain());
            ScanProjectEntity oldProject = scanProjectDao.selectById(dto.getProjectId());
            if (oldProject == null) {
                redisLock.delDomainRedis(dto.getProjectId(), dto.getHost(), dto.getSubDomain(), dto.getScanPorts());
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                log.info("项目id=" + dto.getProjectId() + "已被删除,不处理域名" + dto.getSubDomain());
                return;
            }
            stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_SUB_DOMAIN, dto.getSubDomain()), Const.STR_1);
            String ports = tmpRedisService.getHostInfo(dto.getHost()).getScanPorts();
            String vailDayStr = stringRedisTemplate.opsForValue().get(CacheConst.REDIS_VAIL_DAY);
            Integer vailDay = StringUtils.isEmpty(vailDayStr) ? Const.INTEGER_0 : Integer.valueOf(vailDayStr);
            if (PortUtils.portEquals(ports, dto.getScanPorts()) && DateUtils.isInTwoWeek(dto.getScanTime(), new Date(), vailDay)) {
                redisLock.delDomainRedis(dto.getProjectId(), dto.getHost(), dto.getSubDomain(), dto.getScanPorts());
                log.info(dto.getSubDomain() + "该子域名的主域名已全部扫描完毕!");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                return;
            }
            List<String> ipList = dto.getSubIpList();
            List<Long> ipLongList = ipList.stream().map(s -> IpLongUtils.ipToLong(s)).collect(Collectors.toList());

            String projectStr = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_PROJECT, dto.getProjectId()));
            ScanProjectEntity redisProject = StringUtils.isEmpty(projectStr) ? new ScanProjectEntity() : JSON.parseObject(projectStr, ScanProjectEntity.class);
            String parentDomain = RexpUtil.getMajorDomain(dto.getHost());
            String company = tmpRedisService.getHostInfo(dto.getHost()).getCompany();
            List<ScanHostEntity> exitIpEntityList = scanHostService.getByIpList(ipLongList, dto.getSubDomain());
            Map<Long, ScanHostEntity> exitIpMap = exitIpEntityList.stream().collect(Collectors.toMap(ScanHostEntity::getIpLong, ip -> ip));
            List<ScanParamDto> scanPortParamList = new ArrayList<>();
            List<ScanHostEntity> saveHostList = new ArrayList<>();
            List<ScanHostEntity> updateHostList = new ArrayList<>();
//            String scanPorts = PortUtils.getNewPorts(ports, dto.getScanPorts());
            String scanPorts = dto.getScanPorts();
            String allPorts = PortUtils.getAllPorts(ports, dto.getScanPorts());
            Date now = new Date();
            if (!CollectionUtils.isEmpty(ipList)) {
                for (String ip : ipList) {
                    // 扫描端口ip
                    if (Const.INTEGER_1.equals(dto.getPortFlag())) {
                        ScanParamDto ipDto = ScanParamDto.builder()
                                .projectId(dto.getProjectId())
                                .host(dto.getHost()).subDomain(dto.getSubDomain())
                                .subIp(ip).scanPorts(scanPorts).allPorts(allPorts)
                                .scanTime(dto.getScanTime())
                                .portTool(redisProject == null ? Const.INTEGER_1 : redisProject.getPortTool())
                                .build();
                        scanPortParamList.add(ipDto);
                    }
                    // 新的域名与ip组合
                    Long ipLong = IpLongUtils.ipToLong(ip);
                    if (!CollectionUtils.isEmpty(exitIpMap) && exitIpMap.containsKey(ipLong)) {
                        ScanHostEntity host = exitIpMap.get(ipLong);
                        host.setUpdateTime(now);
                        updateHostList.add(host);
                        exitIpMap.remove(ipLong);
                    } else {
                        ScanHostEntity host = ScanHostEntity.builder()
                                .parentDomain(parentDomain)
                                .domain(dto.getSubDomain())
                                .ip(ip).ipLong(ipLong)
                                .company(company)
                                .type(Const.INTEGER_3)
                                .isMajor(RexpUtil.isMajorDomain(dto.getSubDomain()) ? Const.INTEGER_1 : Const.INTEGER_0)
                                .isDomain(Const.INTEGER_1)
                                .isScanning(Const.INTEGER_1)
                                .subIpList(ipList)
                                .build();
                        saveHostList.add(host);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(saveHostList)) {
                scanHostService.saveBatch(saveHostList);
            }
            List<Long> delIds = new ArrayList<>();
            if (!CollectionUtils.isEmpty(exitIpMap)) {
                Collection<ScanHostEntity> delList = exitIpMap.values();
                delIds = delList.stream().map(ScanHostEntity::getHostId).collect(Collectors.toList());
            }
            if (!CollectionUtils.isEmpty(updateHostList) || !CollectionUtils.isEmpty(delIds)) {
                String lockKey = String.format(CacheConst.REDIS_LOCK_UPDATE_HOST, dto.getSubDomain());
                RLock lock = redisson.getLock(lockKey);
                try {
                    lock.lock();
                    if (!CollectionUtils.isEmpty(updateHostList)) {
                        scanHostService.updateBatch(updateHostList);
                    }
                    if (!CollectionUtils.isEmpty(delIds)) {
                        scanHostService.removeByIds(delIds);
                    }
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
            scanPortParamList.stream().forEach(d -> {
                try {
                    scanPortInfoService.scanSingleIpPortList(d);
                } catch (Exception e) {
                    log.info("扫描端口异常", e);
                }
            });

            // 扫描url、title、cms
            scanHostPortService.scanSingleHostPortList(dto.getSubDomain());
            if (Const.INTEGER_1.equals(redisProject.getNucleiFlag()) || Const.INTEGER_1.equals(redisProject.getAfrogFlag()) || Const.INTEGER_1.equals(redisProject.getXrayFlag())) {
                List<Integer> portList = scanPortService.queryPortList(dto.getSubDomain());
                if (!CollectionUtils.isEmpty(portList)) {
                    for (Integer port : portList) {
                        ScanParamDto holeDto = ScanParamDto.builder()
                                .projectId(dto.getProjectId()).domain(dto.getHost())
                                .subDomain(dto.getSubDomain()).scanPorts(dto.getScanPorts())
                                .port(port)
                                .build();
                        holeSender.sendHoleToMqtt(holeDto);
                    }
                }
            } else {
                redisLock.delDomainRedis(dto.getProjectId(), dto.getHost(), dto.getSubDomain(), dto.getScanPorts());
            }
            stringRedisTemplate.delete(String.format(CacheConst.REDIS_SCANNING_SUB_DOMAIN, dto.getSubDomain()));
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            log.info("处理项目域名完毕:" + dto.getProjectId() + "域名：" + dto.getSubDomain());
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.error("子域名:" + dto.getSubDomain() + "处理异常", e);
            } catch (IOException ioException) {
                log.error("产生异常的参数", ioException);
            }
        }
    }

}
