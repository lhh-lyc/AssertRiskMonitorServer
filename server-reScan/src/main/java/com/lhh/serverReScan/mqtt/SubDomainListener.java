package com.lhh.serverReScan.mqtt;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverReScan.dao.ScanHostDao;
import com.lhh.serverReScan.dao.ScanProjectHostDao;
import com.lhh.serverReScan.service.ScanHostService;
import com.lhh.serverReScan.service.ScanPortInfoService;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ReScanDto;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.serverbase.utils.RexpUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(value = "reScanSubDomain", durable = "true", autoDelete = "false", exclusive = "false"),
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
    private StringRedisTemplate stringRedisTemplate;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ReScanDto dto = (ReScanDto) SerializationUtils.deserialize(bytes);
        String parentDomain = dto.getParentDomain();
        try {
            if (!CollectionUtils.isEmpty(dto.getHostList())) {
                String company = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_DOMAIN_COMPANY, parentDomain));
                company = StringUtils.isEmpty(company) ? Const.STR_CROSSBAR : company;
                for (String domain : dto.getHostList()) {
                    List<String> ipList = getDomainIpList(domain);
                    if (!CollectionUtils.isEmpty(ipList)) {
                        log.info("重新扫描:"+String.join(Const.STR_COMMA, ipList));
                        // 扫描出端口的才保存
                        List<String> valiIpList = new ArrayList<>();
                        ipList.stream().forEach(ip -> {
                            //业务处理
                            Boolean flag = scanPortInfoService.scanSingleIpPortList(domain, ip);
                            if (flag) {
                                valiIpList.add(ip);
                            }
                        });
                        //
                        List<ScanHostEntity> exitHostList = scanHostDao.queryByDomain(domain);
                        List<Long> exitIpList = exitHostList.stream().map(ScanHostEntity::getIpLong).collect(Collectors.toList());
                        List<Long> ipParam = valiIpList.stream().map(Long::valueOf).collect(Collectors.toList());
                        ipParam.removeAll(exitIpList);
                        if (!CollectionUtils.isEmpty(ipParam)) {
                            List<ScanHostEntity> saveList = new ArrayList<>();
                            for (Long ip : ipParam) {
                                   ScanHostEntity host = ScanHostEntity.builder()
                                           .parentDomain(parentDomain).domain(domain)
                                           .ipLong(ip).scanPorts(Const.STR_1_65535).company(company)
                                           .isMajor(Const.INTEGER_0).isDomain(Const.INTEGER_1).isScanning(Const.INTEGER_0)
                                           .build();
                                   saveList.add(host);
                            }
                            scanHostService.saveBatch(saveList);
                        }
                    }
                }
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                log.error("host" + parentDomain + "处理异常", e);
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
