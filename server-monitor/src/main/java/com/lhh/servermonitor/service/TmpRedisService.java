package com.lhh.servermonitor.service;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.CopyUtils;
import com.lhh.serverbase.utils.DomainIpUtils;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.dao.HostCompanyDao;
import com.lhh.servermonitor.mqtt.MqHostSender;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.JedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TmpRedisService {

    @Autowired
    HostCompanyDao hostCompanyDao;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public String getDomainScanPorts(String domain) {
        String scanPorts = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_DOMAIN_SCANPORTS, domain));
        if (StringUtils.isEmpty(scanPorts)) {
            HostCompanyEntity hostInfo = hostCompanyDao.queryByHost(domain);
            String ports = hostInfo == null ? Const.STR_CROSSBAR : StringUtils.isEmpty(hostInfo.getScanPorts()) ? Const.STR_CROSSBAR : hostInfo.getScanPorts();
            stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_DOMAIN_SCANPORTS, domain), ports, 60 * 60 * 12, TimeUnit.SECONDS);
            scanPorts = ports;
        }
        return scanPorts;
    }

}
