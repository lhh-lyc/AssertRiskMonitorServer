package com.lhh.serverTask.mqtt;

import com.lhh.serverTask.dao.ScanHostDao;
import com.lhh.serverTask.dao.ScanProjectHostDao;
import com.lhh.serverTask.service.ScanHostPortService;
import com.lhh.serverTask.service.ScanHostService;
import com.lhh.serverTask.service.ScanPortInfoService;
import com.lhh.serverTask.utils.RedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ReScanDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.RexpUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(value = "taskScanSubDomain", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class SubDomainListener {

    @Autowired
    ScanHostDao scanHostDao;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    ScanProjectHostDao scanProjectHostDao;
    @Resource
    private ScanHostService scanHostService;
    @Resource
    private ScanHostPortService scanHostPortService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedisUtils redisUtils;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ReScanDto dto = (ReScanDto) SerializationUtils.deserialize(bytes);
        String parentDomain = dto.getParentDomain();
        try {
            if (!CollectionUtils.isEmpty(dto.getHostList())) {
                Boolean doneFlg = true;
                for (String domain : dto.getHostList()) {
                    Boolean flg = stringRedisTemplate.hasKey(String.format(CacheConst.REDIS_END_HOST_PORT, domain));
                    if (!flg) {
                        doneFlg = false;
                        break;
                    }
                }
                if (doneFlg) {
                    for (String domain : dto.getHostList()) {
                        stringRedisTemplate.delete(String.format(CacheConst.REDIS_END_HOST_PORT, domain));
                    }
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                    log.info("host:" + parentDomain + "已被消费，移出队列");
                    return;
                }
                String company = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_DOMAIN_COMPANY, parentDomain));
                company = StringUtils.isEmpty(company) ? Const.STR_CROSSBAR : company;
                for (String domain : dto.getHostList()) {
                    List<String> ipList = RexpUtil.isIP(domain) ? new ArrayList<>(Arrays.asList(domain)) : getDomainIpList(domain);
                    if (!CollectionUtils.isEmpty(ipList)) {
                        log.info("重新扫描:"+String.join(Const.STR_COMMA, ipList));
                        // 扫描出端口的才保存
                        List<String> valiIpList = new ArrayList<>();
                        ipList.stream().forEach(ip -> {
                            //业务处理
                            Boolean flag = scanPortInfoService.scanSingleIpPortList(parentDomain, domain, ip);
                            if (flag) {
                                valiIpList.add(ip);
                            }
                        });
                        //
                        List<ScanHostEntity> exitHostList = scanHostDao.queryByDomain(domain);
                        List<Long> exitIpList = exitHostList.stream().map(ScanHostEntity::getIpLong).collect(Collectors.toList());
                        List<Long> ipParam = valiIpList.stream().map(i->IpLongUtils.ipToLong(i)).collect(Collectors.toList());
                        ipParam.removeAll(exitIpList);
                        if (!CollectionUtils.isEmpty(ipParam)) {
                            List<ScanHostEntity> saveList = new ArrayList<>();
                            for (Long ip : ipParam) {
                                   ScanHostEntity host = ScanHostEntity.builder()
                                           .parentDomain(parentDomain).domain(domain).type(Const.INTEGER_3)
                                           .ipLong(ip).scanPorts(Const.STR_1_65535).company(company)
                                           .isMajor(Const.INTEGER_0).isDomain(Const.INTEGER_1).isScanning(Const.INTEGER_0)
                                           .build();
                                   saveList.add(host);
                            }
                            scanHostService.saveBatch(saveList);
                        }
                    }
                    // 重新解析cms、url、title
                    scanHostPortService.scanSingleHostPortList(domain);
                }
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                for (String domain : dto.getHostList()) {
                    stringRedisTemplate.delete(String.format(CacheConst.REDIS_END_HOST_PORT, domain));
                }
                log.info("host:" + parentDomain + "处理完毕");
            }
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.error("host:" + parentDomain + "处理异常", e);
            } catch (IOException ioException) {
                log.error("产生异常的参数",ioException);
            }
        }
    }

    /**
     * java代码解析子域名ip
     */
    private List<String> getDomainIpList(String domain) {
        List<String> list = new ArrayList<>();
        try {
            InetAddress[] inetadd = InetAddress.getAllByName(domain);
            //遍历所有的ip并输出
            for (int i = 0; i < inetadd.length; i++) {
                if (!StringUtils.isEmpty(inetadd[i] + Const.STR_EMPTY)) {
                    String ip = (inetadd[i] + Const.STR_EMPTY).split(Const.STR_SLASH)[1];
                    if (RexpUtil.isIP(ip)) {
                        list.add(ip);
                    }
                }
            }
            String ips = CollectionUtils.isEmpty(list) ? Const.STR_EMPTY : String.join(Const.STR_COMMA, list);
            log.info(domain + (CollectionUtils.isEmpty(list) ? "未解析出ip" : "解析ip为：" + ips));
        } catch (UnknownHostException e) {
            list.add(Const.STR_CROSSBAR);
            log.error(domain + "解析ip出错");
        }
        return list;
    }

}
