package com.lhh.servermonitor.service;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanAddRecordEntity;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.CopyUtils;
import com.lhh.serverbase.utils.DomainIpUtils;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.servermonitor.controller.RedisLock;
import com.lhh.servermonitor.mqtt.HostSender;
import com.lhh.servermonitor.utils.ExecUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
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
    ScanAddRecordService scanAddRecordService;
    @Autowired
    HostCompanyService hostCompanyService;
    @Autowired
    TmpRedisService tmpRedisService;
    @Autowired
    RedisLock redisLock;
    @Autowired
    HostSender mqHostSender;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redisson;

    public List<String> getSubDomainTest(String domain) {
        List<String> subdomainList = new ArrayList<>();
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
        return subdomainList;
    }

    public List<Map> nucleiTest(String requestUrl) {
        log.info(requestUrl + "---nuclei漏洞扫描");
        String cmd = String.format(Const.STR_NUCLEI, toolDir, Const.STR_EMPTY, requestUrl);
        SshResponse response = null;
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info(requestUrl);
        String outStr = RexpUtil.removeColor(response.getOut());
        log.info(outStr);
        List<String> responseLineList = Arrays.asList(outStr.split("\n"));
        List<String> serverLineList = responseLineList.stream().filter(r -> r.startsWith("[") && !r.startsWith("[INF]")).collect(Collectors.toList());
        List<Map> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(serverLineList)) {
            for (String str : serverLineList) {
                String[] list = str.split(Const.STR_BLANK);
                String server = list[0].substring(1, list[0].length() - 1);
                String h = list[1].substring(1, list[1].length() - 1);
                String level = list[2].substring(1, list[2].length() - 1);
                String url = list[3];
                String f = list.length > 4 ? list[4].substring(1, list[4].length() - 1) : Const.STR_CROSSBAR;
                Map m = new HashMap();
                m.put("s", server);
                m.put("h", h);
                m.put("l", level);
                m.put("u", url);
                m.put("f", f);
                result.add(m);
            }
        }
        return result;
    }

    public void scanDomainList(ScanParamDto scanDto) {
        List<ScanParamDto> subdomainList = getSubDomainList(scanDto.getProjectId(), scanDto.getHost(), scanDto.getSubDomainFlag(), scanDto.getScanPorts());
        List<ScanProjectHostEntity> saveList = new ArrayList<>();
        List<ScanProjectHostEntity> updateList = new ArrayList<>();
        List<ScanAddRecordEntity> recordList = new ArrayList<>();
        List<ScanParamDto> dtoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(subdomainList)) {
            List<ScanProjectHostEntity> phList = scanProjectHostService.selByProIdAndHost(scanDto.getProjectId(), scanDto.getHost());
//            Map<String, ScanProjectHostEntity> phMap = phList.stream().collect(Collectors.toMap(ScanProjectHostEntity::getHost, ph -> ph));
            Map<String, List<ScanProjectHostEntity>> phMap = phList.stream().collect(Collectors.groupingBy(ScanProjectHostEntity::getHost));
            Date now = new Date();
            for (ScanParamDto subdomain : subdomainList) {
                // 保存项目-host关联关系
                if (!phMap.containsKey(subdomain.getHost())) {
                    ScanProjectHostEntity item = ScanProjectHostEntity.builder()
                            .projectId(scanDto.getProjectId())
                            .parentDomain(scanDto.getHost().equals(subdomain.getHost()) ? RexpUtil.getMajorDomain(subdomain.getHost()) : scanDto.getHost())
                            .host(subdomain.getHost())
                            .isScanning(Const.INTEGER_1)
                            .build();
                    saveList.add(item);

                    // 新增扫描子域名记录
                    ScanAddRecordEntity record = ScanAddRecordEntity.builder()
                            .projectId(scanDto.getProjectId()).parentName(scanDto.getHost())
                            .subName(subdomain.getHost()).addRecordType(Const.INTEGER_1)
                            .build();
                    recordList.add(record);

                    ScanParamDto dto = new ScanParamDto();
                    CopyUtils.copyProperties(scanDto, dto);
                    dto.setSubDomain(subdomain.getHost());
                    dto.setSubIpList(subdomain.getSubIpList());
                    dtoList.add(dto);
                } else {
                    List<ScanProjectHostEntity> phs = phMap.get(subdomain.getHost());
                    for (ScanProjectHostEntity ph : phs) {
                        ph.setUpdateTime(now);
                        updateList.add(ph);
                    }
                    phMap.remove(subdomain.getHost());
                }
            }
            if (!CollectionUtils.isEmpty(saveList)) {
                scanProjectHostService.saveBatch(saveList);
            }
            if (!CollectionUtils.isEmpty(recordList)) {
                scanAddRecordService.saveBatch(recordList);
            }
            List<Long> delIds = new ArrayList<>();
            if (!CollectionUtils.isEmpty(phMap)) {
                Collection<ScanProjectHostEntity> delList = new ArrayList<>();
                for (String key : phMap.keySet()) {
                    List<ScanProjectHostEntity> list = phMap.get(key);
                    List<Long> ids = list.stream().map(ScanProjectHostEntity::getId).collect(Collectors.toList());
                    delIds.addAll(ids);
                }
            }
            if (!CollectionUtils.isEmpty(updateList) || !CollectionUtils.isEmpty(delIds)) {
                String lockKey = String.format(CacheConst.REDIS_LOCK_UPDATE_PROJECT_HOST, scanDto.getProjectId(), scanDto.getHost());
                RLock lock = redisson.getLock(lockKey);
                try {
                    lock.lock();
                    if (!CollectionUtils.isEmpty(updateList)) {
                        scanProjectHostService.updateBatch(updateList);
                    }
                    if (!CollectionUtils.isEmpty(delIds)) {
                        scanProjectHostService.removeByIds(delIds);
                    }
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
            mqHostSender.sendScanningHostToMqtt(dtoList);
        } else {
            // 未扫描出子域名或者子域名未解析出ip，主域名结束流程
            log.info(scanDto.getHost() + "没有有效子域名");
            redisLock.removeProjectRedis(scanDto.getProjectId(), scanDto.getHost());
        }
    }

    public List<ScanParamDto> getSubDomainList(Long projectId, String domain, Integer subDomainFlag, String scanPorts) {
        List<ScanParamDto> result;
        Integer n = Const.INTEGER_10;
        Boolean manyFlg = true;
        List<String> blackIpList = new ArrayList<>();
        log.info("开始判断" + domain + "是否为泛解析主域名");
        for (int i = 0; i < n; i++) {
            String pre = RandomStringUtils.random(Const.INTEGER_10, Const.STR_LETTERS);
            List<String> list = DomainIpUtils.getRandomDomainIpList(pre + Const.STR_DOT + domain);
            blackIpList.addAll(list);
            if (Const.INTEGER_1.equals(list.size()) && list.get(0).equals(Const.STR_CROSSBAR)) {
                manyFlg = false;
                log.info(domain + "不是泛解析主域名");
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
        if (!subdomainList.contains(domain)) {
            subdomainList.add(domain);
        }

        if (manyFlg && subdomainList.size() > Const.INTEGER_50) {
            if (subdomainList.size() > 5000) {
                log.info(domain + "子域名数量过多！");
            }
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
     *
     * @param domain
     * @param list
     * @param blackIpList
     * @return
     */
    public List<ScanParamDto> dealList(Long projectId, String scanPorts, String domain, List<String> list, List<String> blackIpList) {
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
                Map<String, Integer> tmpIpNumMap = (Map<String, Integer>) future.get().get("ipNumMap");
                List<ScanParamDto> tmpDtoList = (List<ScanParamDto>) future.get().get("dtoList");
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
        String company = tmpRedisService.getHostInfo(domain).getCompany();
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
