package com.lhh.servermonitor.service;

import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanHostPortEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.servermonitor.dao.ScanHostPortDao;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.HttpscanCustomizeUtils;
import com.lhh.servermonitor.utils.HttpxCustomizeUtils;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ScanHostPortService {

    @Value("${dir-setting.tool-dir}")
    private String toolDir;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ScanPortService scanPortService;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanHostPortDao scanHostPortDao;
    @Autowired
    ScanProjectHostService scanProjectHostService;
    @Autowired
    RedissonClient redisson;
    @Autowired
    HttpxCustomizeUtils httpxCustomizeUtils;
    @Autowired
    HttpscanCustomizeUtils httpscanCustomizeUtils;

    /**
     * 解析url请求title/cms
     *
     * @param domain
     */
    public void scanSingleHostPortList(String domain) {
        String lockKey = String.format(CacheConst.REDIS_LOCK_HOST_PORT, domain);
        RLock lock = redisson.getLock(lockKey);
        boolean success = true;
        try {
            success = lock.tryLock();
            if (success) {
                List<String> urlList = new ArrayList<>();
                List<Integer> portList = scanPortService.queryPortList(domain);
                if (!CollectionUtils.isEmpty(portList)) {
                    List<ScanHostPortEntity> saveList = new ArrayList<>();
                    for (Integer port : portList) {
                        urlList.add(domain + Const.STR_COLON + port);
                    }
                    String urls = String.join("\\\\" + "n", urlList);
                    Long projectId = 1L;
                    String createCmd = String.format(Const.STR_CREATE_HTTPX_URLS, toolDir, urls, projectId + Const.STR_UNDERLINE + domain);
                    String cmd = String.format(Const.STR_HTTPSCAN_URLS, toolDir, projectId + Const.STR_UNDERLINE + domain);
                    String delCmd = String.format(Const.STR_DEL_HTTPX_URLS, toolDir, toolDir, projectId + Const.STR_UNDERLINE + domain);
                    SshResponse response;
                    try {
//                        log.info("执行命令：" + createCmd);
                        ExecUtil.runCommand(createCmd);
                        log.info("执行命令：" + cmd);
                        response = ExecUtil.runCommand(cmd);
                        List<String> responseLineList = Arrays.asList(response.getOut().split("\n"));
                        if (!CollectionUtils.isEmpty(responseLineList)) {
                            for (String line : responseLineList) {
                                if (StringUtils.isEmpty(line)) {
                                    continue;
                                }
                                String[] arr = line.split("\t");
                                String title = Const.STR_CROSSBAR;
                                String url = arr[0];
                                String port = url.substring(url.lastIndexOf(Const.STR_COLON) + 1);
                                if (arr.length > 1 && !"[]".equals(arr[1])) {
                                    title = arr[1].substring(2, arr[1].length() - 2);
                                }
                                String cms = httpscanCustomizeUtils.getUrlCms(toolDir, url);
                                ScanHostPortEntity entity = ScanHostPortEntity.builder()
                                        .domain(domain).port(Integer.valueOf(port))
                                        .url(url)
                                        .title(title)
                                        .cms(cms)
                                        .build();
                                saveList.add(entity);
                            }
                        }
                        scanHostPortDao.delByDomain(domain);
                        if (!CollectionUtils.isEmpty(saveList)) {
                            scanHostPortDao.saveBatch(saveList);
                        }
//                        log.info("执行命令：" + delCmd);
                        ExecUtil.runCommand(delCmd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                /*List<Integer> portList = scanPortService.queryWebPortList(domain);
                if (!CollectionUtils.isEmpty(portList)) {
                    List<ScanHostPortEntity> saveList = new ArrayList<>();
                    for (Integer port : portList) {
                        Map<String, String> result = null;
                        try {
                            result = httpxCustomizeUtils.getUrlMap(stringRedisTemplate, toolDir, domain + Const.STR_COLON + port);
                        } catch (IOException e) {
                            log.error("请求错误：" + domain + Const.STR_COLON + port, e);
                            e.printStackTrace();
                        }
                        if (!CollectionUtils.isEmpty(result)) {
                            ScanHostPortEntity entity = ScanHostPortEntity.builder()
                                    .domain(domain).port(port)
                                    .url(result.get("url"))
                                    .title(result.get("title"))
                                    .cms(result.get("cms"))
                                    .build();
                            saveList.add(entity);
                        }
                    }
                    scanHostPortDao.delByDomain(domain);
                    if (!CollectionUtils.isEmpty(saveList)) {
                        scanHostPortDao.saveBatch(saveList);
                    }
                }*/
            }
        } catch (Exception e) {
            log.error("更新scan_host_port表" + domain + "出错", e);
        } finally {
            if (success && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 解析url请求title/cms
     *
     * @param domain
     */
    public void scanHostPortList(String domain) {
        String lockKey = String.format(CacheConst.REDIS_LOCK_HOST_PORT, domain);
        RLock lock = redisson.getLock(lockKey);
        boolean success = true;
        try {
            success = lock.tryLock();
            if (success) {
                List<String> urlList = new ArrayList<>();
                List<Integer> portList = scanPortService.queryPortList(domain);
                if (!CollectionUtils.isEmpty(portList)) {
                    urlList.add("http://" + domain + Const.STR_COLON + portList);
                    urlList.add("https://" + domain + Const.STR_COLON + portList);
                    List<ScanHostPortEntity> saveList = new ArrayList<>();
                    for (Integer port : portList) {
                        Map<String, String> result = null;
                        try {
                            result = httpxCustomizeUtils.getUrlMap(stringRedisTemplate, toolDir, domain + Const.STR_COLON + port);
                        } catch (IOException e) {
                            log.error("请求错误：" + domain + Const.STR_COLON + port, e);
                            e.printStackTrace();
                        }
                        if (!CollectionUtils.isEmpty(result)) {
                            ScanHostPortEntity entity = ScanHostPortEntity.builder()
                                    .domain(domain).port(port)
                                    .url(result.get("url"))
                                    .title(result.get("title"))
                                    .cms(result.get("cms"))
                                    .build();
                            saveList.add(entity);
                        }
                    }
                    scanHostPortDao.delByDomain(domain);
                    if (!CollectionUtils.isEmpty(saveList)) {
                        scanHostPortDao.saveBatch(saveList);
                    }
                }
            }
        } catch (Exception e) {
            log.error("更新scan_host_port表" + domain + "出错", e);
        } finally {
            if (success && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
