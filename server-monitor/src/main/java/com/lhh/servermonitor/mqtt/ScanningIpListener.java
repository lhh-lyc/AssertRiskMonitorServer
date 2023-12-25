package com.lhh.servermonitor.mqtt;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.dao.ScanProjectDao;
import com.lhh.servermonitor.service.*;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(value = "scanningIpData", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ScanningIpListener {

    @Autowired
    SysDictService sysDictService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    ScanHostPortService scanHostPortService;
    @Autowired
    ScanHoleService scanHoleService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    HostCompanyService hostCompanyService;
    @Autowired
    TmpRedisService tmpRedisService;
    @Autowired
    private ScanProjectDao scanProjectDao;
    @Autowired
    HoleSender holeSender;
    @Autowired
    RedisLock redisLock;
    @Autowired
    RedissonClient redisson;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ScanParamDto dto = (ScanParamDto) SerializationUtils.deserialize(bytes);
        try {
            log.info("扫描ip端口：" + JSON.toJSONString(dto));
            ScanProjectEntity oldProject = scanProjectDao.selectById(dto.getProjectId());
            if (oldProject == null) {
                redisLock.delDomainRedis(dto.getProjectId(), dto.getSubIp(), dto.getSubIp(), dto.getScanPorts());
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                log.info("项目id=" + dto.getProjectId() + "已被删除,不处理ip端口" + dto.getSubIp());
                return;
            }
            String company = tmpRedisService.getHostInfo(dto.getSubIp()).getCompany();
            List<ScanHostEntity> exitIpList = scanHostService.getByIpList(Arrays.asList(IpLongUtils.ipToLong(dto.getSubIp())), dto.getSubIp());
            if (CollectionUtils.isEmpty(exitIpList)) {
                ScanHostEntity scanIp = ScanHostEntity.builder()
                        .domain(dto.getSubIp()).parentDomain(dto.getSubIp())
                        .ip(dto.getSubIp()).ipLong(IpLongUtils.ipToLong(dto.getSubIp()))
                        .company(company)
                        .type(Const.INTEGER_2)
                        .isMajor(Const.INTEGER_0)
                        .isDomain(Const.INTEGER_0)
                        .isScanning(Const.INTEGER_0)
                        .build();
                scanHostService.save(scanIp);
            } else {
                ScanHostEntity host = exitIpList.get(0);
                host.setUpdateTime(new Date());
                String lockKey = String.format(CacheConst.REDIS_LOCK_UPDATE_HOST, dto.getSubIp());
                RLock lock = redisson.getLock(lockKey);
                try {
                    lock.lock();
                    scanHostService.updateById(host);
                } finally {
                    lock.unlock();
                }
            }

            ScanParamDto ipDto = ScanParamDto.builder()
                    .projectId(dto.getProjectId())
                    .host(dto.getSubIp()).subDomain(dto.getSubIp())
                    .subIp(dto.getSubIp()).scanPorts(dto.getScanPorts()).allPorts(dto.getAllPorts())
                    .scanTime(dto.getScanTime())
                    .portTool(dto.getPortTool())
                    .build();
            scanPortInfoService.scanIpsPortList(ipDto);
            scanHostPortService.scanSingleHostPortList(dto.getSubIp());
            String projectStr = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_PROJECT, dto.getProjectId()));
            ScanProjectEntity redisProject = JSON.parseObject(projectStr, ScanProjectEntity.class);
            if (Const.INTEGER_1.equals(redisProject.getNucleiFlag()) || Const.INTEGER_1.equals(redisProject.getAfrogFlag()) || Const.INTEGER_1.equals(redisProject.getXrayFlag())) {
                ScanParamDto holeDto = ScanParamDto.builder()
                        .projectId(dto.getProjectId()).domain(dto.getSubIp())
                        .subDomain(dto.getSubIp()).scanPorts(dto.getScanPorts())
                        .build();
                holeSender.sendHoleToMqtt(holeDto);
            } else {
                redisLock.delDomainRedis(dto.getProjectId(), dto.getSubIp(), dto.getSubIp(), dto.getScanPorts());
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.error("扫描ip端口失败：" + e);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
