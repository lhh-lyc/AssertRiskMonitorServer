package com.lhh.servermonitor.service;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.CopyUtils;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.mqtt.MqHostSender;
import com.lhh.servermonitor.utils.DomainIpUtils;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.JedisUtils;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScanService {

    @Value("${dir-setting.tool-dir}")
    private String toolDir;

    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    ScanProjectContentService scanProjectContentService;
    @Autowired
    ScanPortInfoService scanPortInfoService;
    @Autowired
    RedisLock redisLock;
    @Autowired
    MqHostSender mqHostSender;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public void scanDomainList2(ScanParamDto scanDto) {
        List<ScanParamDto> subdomainList = getSubDomainList(scanDto.getProjectId(), scanDto.getHost(), scanDto.getSubDomainFlag(), scanDto.getScanPorts());
        List<ScanProjectHostEntity> projectHostList = new ArrayList<>();
        List<ScanParamDto> dtoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(subdomainList)) {
            // 查询域名关联是否已存在，防止服务重启等情况导致一个项目多个相同域名关联
            List<ScanProjectHostEntity> phList = scanProjectHostService.selByProIdAndHost(scanDto.getProjectId(), Const.STR_EMPTY);
            List<String> exitPhList = phList.stream().map(ScanProjectHostEntity::getHost).collect(Collectors.toList());
            for (ScanParamDto subdomain : subdomainList) {
                // 保存项目-host关联关系
                if (!exitPhList.contains(subdomain.getHost())) {
                    ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                            .projectId(scanDto.getProjectId())
                            .parentDomain(scanDto.getHost().equals(subdomain.getHost()) ? RexpUtil.getMajorDomain(subdomain.getHost()) : scanDto.getHost())
                            .host(subdomain.getHost())
                            .isScanning(Const.INTEGER_1)
                            .build();
                    projectHostList.add(item);

                    ScanParamDto dto = new ScanParamDto();
                    CopyUtils.copyProperties(scanDto, dto);
                    dto.setSubDomain(subdomain.getHost());
                    dto.setSubIpList(subdomain.getSubIpList());
                    dtoList.add(dto);
                }
            }
            scanProjectHostService.saveBatch(projectHostList);
            mqHostSender.sendScanningHostToMqtt(dtoList);
        } else {
            // 未扫描出子域名或者子域名未解析出ip，主域名结束流程
            log.info(scanDto.getHost() + "没有有效子域名");
            redisLock.removeProjectRedis(scanDto.getProjectId(), scanDto.getHost());
        }
    }

    public List<ScanParamDto> getSubDomainList(Long projectId, String domain, Integer subDomainFlag, String scanPorts){
        List<ScanParamDto> result;
        Integer n = Const.INTEGER_10;
        Boolean manyFlg = true;
        List<String> blackIpList = new ArrayList<>();
        log.info("开始判断" + domain + "是否为泛解析主域名");
        for (int i=0;i<n;i++) {
            String pre = RandomStringUtils.random(Const.INTEGER_10, Const.STR_LETTERS);
            List<String> list = DomainIpUtils.getDomainIpList(pre + Const.STR_DOT + domain);
            blackIpList.addAll(list);
            if (Const.INTEGER_1.equals(list.size()) && list.get(0).equals(Const.STR_CROSSBAR)) {
                manyFlg = false;
                break;
            }
        }
        blackIpList = blackIpList.stream().distinct().collect(Collectors.toList());
        List<String> subdomainList = new ArrayList<>();
        if (Const.INTEGER_1.equals(subDomainFlag)) {
            log.info(domain + "子域名收集");
            // 子域名列表
            String cmd = String.format(Const.STR_SUBFINDER_SUBDOMAIN, toolDir, domain);
            SshResponse response = null;
            try {
                response = ExecUtil.runCommand(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            subdomainList = response.getOutList();
            subdomainList = subdomainList.stream().distinct().collect(Collectors.toList());
            log.info(CollectionUtils.isEmpty(subdomainList) ? "执行工具命令返回" + JSON.toJSONString(response) + ";" + domain + "未扫描到子域名" : domain + "子域名有" + subdomainList.size() + "个:" + String.join(Const.STR_COMMA, subdomainList));
        }
        if (!CollectionUtils.isEmpty(subdomainList) && !subdomainList.contains(domain)) {
            subdomainList.add(domain);
        }

        if (manyFlg && subdomainList.size() > Const.INTEGER_50) {
            // 泛解析
            log.info(domain + "为泛解析主域名，ip黑名单为：" + JSON.toJSONString(blackIpList));
            result = dealList(projectId, scanPorts, domain, subdomainList, blackIpList);
        } else {
            // 泛解析
            log.info(domain + "不一定是泛解析主域名");
            result = dealList(projectId, scanPorts, domain, subdomainList, new ArrayList<>());
        }
        return result;
    }

    /**
     * 多线程处理子域名ip
     * @param domain
     * @param list
     * @param blackIpList
     * @return
     */
    public List<ScanParamDto> dealList(Long projectId, String scanPorts, String domain, List<String> list, List<String> blackIpList){
        // 将List划分成10个子任务
        int numThreads = Const.INTEGER_10;
        int batchSize = list.size() / numThreads;
        List<List<String>> subLists = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            int start = i * batchSize;
            int end = (i == numThreads - 1) ? list.size() : (i + 1) * batchSize;
            subLists.add(list.subList(start, end));
        }

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // 提交子任务给线程池
        List<Future<Map<String, Object>>> futures = new ArrayList<>();
        for (List<String> subList : subLists) {
            Future<Map<String, Object>> future = executor.submit(() -> {
                Map<String, Object> result = new HashMap<>();
                Map<String, Integer> ipNumMap = new HashMap<>();
                List<ScanParamDto> dtoList = new ArrayList<>();
                for (String subDomain : subList) {
                    // 提取各个ip的解析数量
                    List<String> ipList = DomainIpUtils.getDomainIpList(subDomain);
                    // 剔除黑名单内的ip域名
                    if (Collections.disjoint(blackIpList, ipList)) {
                        for (String ip : ipList) {
                            Integer num = ipNumMap.get(ip) == null ? Const.INTEGER_1 : ipNumMap.get(ip) + 1;
                            ipNumMap.put(ip, num);
                        }
                        ScanParamDto dto = ScanParamDto.builder()
                                .host(subDomain).subIpList(ipList)
                                .build();
                        dtoList.add(dto);
                    }
                }
                result.put("ipNumMap", ipNumMap);
                result.put("dtoList", dtoList);
                return result;
            });
            futures.add(future);
        }

        // 等待所有子任务完成
        Map<String, Integer> ipNumMap = new ConcurrentHashMap<>();
        List<ScanParamDto> dtoList = new CopyOnWriteArrayList<>();
        for (Future<Map<String, Object>> future : futures) {
            try {
                Map<String, Integer> tmpIpNumMap = (Map<String, Integer>)future.get().get("ipNumMap");
                List<ScanParamDto> tmpDtoList = (List<ScanParamDto>)future.get().get("dtoList");
                tmpIpNumMap.forEach((key, value) -> ipNumMap.merge(key, value, Integer::sum));
                dtoList.addAll(tmpDtoList);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        // 关闭线程池
        executor.shutdown();
        // 要丢掉的ip数组
        List<String> ignoreIpList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ipNumMap)) {
            for (String ip : ipNumMap.keySet()) {
                if (ipNumMap.get(ip) > Const.INTEGER_100) {
                    ignoreIpList.add(ip);
                }
            }
        }
        ignoreIpList.remove(Const.STR_CROSSBAR);
        log.info(domain + "子域名总ip数统计(ipNumMap)：" + (CollectionUtils.isEmpty(ipNumMap) ? "[]" : JSON.toJSONString(ipNumMap)));
        log.info(domain + "丢弃的ip有(ignoreIpList)：" + (CollectionUtils.isEmpty(ignoreIpList) ? "[]" : JSON.toJSONString(ignoreIpList)));
        List<ScanParamDto> result = new ArrayList<>();
        List<ScanProjectHostEntity> saveProjectHostList = new ArrayList<>();
        List<ScanHostEntity> saveHostList = new ArrayList<>();
        String company = JedisUtils.getStr(String.format(CacheConst.REDIS_DOMAIN_COMPANY, domain));
        if (!CollectionUtils.isEmpty(dtoList)) {
            for (ScanParamDto dto : dtoList) {
                if (dto.getSubIpList().size() == 1 && dto.getSubIpList().get(0).equals(Const.STR_CROSSBAR)) {
                    // 扫描的子域名没解析到ip，不走后面流程
                    ScanProjectHostEntity ph = ScanProjectHostEntity.builder()
                            .projectId(projectId).parentDomain(domain).host(dto.getHost())
                            .isScanning(Const.INTEGER_0)
                            .build();
                    saveProjectHostList.add(ph);
                    ScanHostEntity h = ScanHostEntity.builder()
                            .parentDomain(domain).domain(dto.getHost())
                            .ipLong(Const.LONG_0).scanPorts(scanPorts)
                            .company(company).type(Const.INTEGER_3)
                            .isMajor(domain.equals(dto.getHost()) ? Const.INTEGER_1 : Const.INTEGER_0)
                            .isDomain(Const.INTEGER_1).isScanning(Const.INTEGER_0)
                            .build();
                    saveHostList.add(h);
                } else {
                    if (Collections.disjoint(ignoreIpList, dto.getSubIpList())) {
                        result.add(dto);
                        log.info(domain + "子域名" + dto.getHost() + "为有效子域名，ip为:" + JSON.toJSONString(dto.getSubIpList()));
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(saveProjectHostList)) {
            scanProjectHostService.saveBatch(saveProjectHostList);
        }
        if (!CollectionUtils.isEmpty(saveHostList)) {
            scanHostService.saveBatch(saveHostList);
        }
        log.info(domain + "一共" + list.size() + "个子域名，筛选出" + result.size() + "个");
        return result;
    }

}
