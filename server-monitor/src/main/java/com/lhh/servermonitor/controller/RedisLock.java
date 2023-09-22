package com.lhh.servermonitor.controller;

import com.alibaba.fastjson2.JSON;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.servermonitor.service.HostCompanyService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RedisLock {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redisson;
    @Autowired
    HostCompanyService hostCompanyService;

    public void saveProjectRedis(ScanProjectEntity project) {
        String lockKey = String.format(CacheConst.REDIS_LOCK_PROJECT, project.getId());
        RLock lock = redisson.getLock(lockKey);
        try {
            lock.lock();
            String projectStr = redisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()));
            if (!StringUtils.isEmpty(projectStr)) {
                ScanProjectEntity redisProject = JSON.parseObject(projectStr, ScanProjectEntity.class);
                List<String> hostList = redisProject.getHostList();
                log.info("项目" + project.getId() + "加主域名前数据：" + redisProject.getHostList());
                hostList.addAll(project.getHostList());
                redisProject.setHostList(hostList.stream().distinct().collect(Collectors.toList()));
                redisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()), JSON.toJSONString(redisProject));
                log.info("项目" + project.getId() + "加主域名后数据：" + JSON.toJSONString(redisProject.getHostList()));
            } else {
                redisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_PROJECT, project.getId()), JSON.toJSONString(project));
                log.info("项目" + project.getId() + "初始化主域名数据：" + JSON.toJSONString(project.getHostList()));
            }
        } catch (Exception e) {
            log.error("项目" + project.getId() + "增加域名报错", e);
        } finally {
            // 判断当前线程是否持有锁
            if (lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
    }

    public void removeProjectRedis(Long projectId, String domain) {
        String lockKey = String.format(CacheConst.REDIS_LOCK_PROJECT, projectId);
        RLock lock = redisson.getLock(lockKey);
        try {
            lock.lock();
            String projectStr = redisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_PROJECT, projectId));
            if (!StringUtils.isEmpty(projectStr)) {
                log.info("项目" + projectId + "移除" + domain + "前数据：" + projectStr);
                ScanProjectEntity redisProject = JSON.parseObject(projectStr, ScanProjectEntity.class);
                List<String> list = redisProject.getHostList();
                list.remove(domain);
                if (CollectionUtils.isEmpty(list)) {
                    redisTemplate.delete(String.format(CacheConst.REDIS_SCANNING_PROJECT, projectId));
                    log.info("项目" + projectId + "扫描完成！");
                } else {
                    redisProject.setHostList(list);
                    redisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_PROJECT, projectId), JSON.toJSONString(redisProject));
                    log.info("项目" + projectId + "移除" + domain + "后数据：" + JSON.toJSONString(redisProject));
                }
            }
        } catch (Exception e) {
            log.error("项目" + projectId + "redis删除域名报错", e);
        } finally {
            // 判断当前线程是否持有锁
            if (lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
    }

    public void addDomainRedis(Long projectId, String domain, String subDomain) {
        String lockKey = String.format(CacheConst.REDIS_LOCK_DOMAIN, domain);
        RLock lock = redisson.getLock(lockKey);
        try {
            lock.lock();
            String subDomains = redisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_DOMAIN, projectId, domain));
            if (!StringUtils.isEmpty(subDomains)) {
                List<String> list = new ArrayList<>(Arrays.asList(subDomains.split(Const.STR_COMMA)));
                if (list.contains(subDomain)) {
                    return;
                }
                list.add(subDomain);
                redisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_DOMAIN, projectId, domain), String.join(Const.STR_COMMA, list));
            } else {
                redisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_DOMAIN, projectId, domain), subDomain);
            }
        } catch (Exception e) {
            log.error(domain + "redis主域名增加子域名报错", e);
        } finally {
            // 判断当前线程是否持有锁
            if (lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
    }

    public void delDomainRedis(Long projectId, String domain, String subDomain, String scanPorts) {
        String lockKey = String.format(CacheConst.REDIS_LOCK_DOMAIN, domain);
        RLock lock = redisson.getLock(lockKey);
        try {
            lock.lock();
            String subDomains = redisTemplate.opsForValue().get(String.format(CacheConst.REDIS_SCANNING_DOMAIN, projectId, domain));
            if (!StringUtils.isEmpty(subDomains)) {
                List<String> list = new ArrayList<>(Arrays.asList(subDomains.split(Const.STR_COMMA)));
                list.remove(subDomain);
                if (CollectionUtils.isEmpty(list)) {
                    removeProjectRedis(projectId, domain);
                    hostCompanyService.updatePorts(domain, scanPorts);
                    redisTemplate.delete(String.format(CacheConst.REDIS_SCANNING_DOMAIN, projectId, domain));
                } else {
                    redisTemplate.opsForValue().set(String.format(CacheConst.REDIS_SCANNING_DOMAIN, projectId, domain), String.join(Const.STR_COMMA, list));
                }
            }
        } catch (Exception e) {
            log.error(domain + "redis主域名删除子域名报错", e);
        } finally {
            // 判断当前线程是否持有锁
            if (lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
    }

}