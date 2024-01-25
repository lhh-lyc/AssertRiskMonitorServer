package com.lhh.serverscanhole.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.serverbase.utils.HttpUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.serverscanhole.dao.HostCompanyDao;
import com.lhh.serverscanhole.service.HostCompanyService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service("hostCompanyService")
public class HostCompanyServiceImpl extends ServiceImpl<HostCompanyDao, HostCompanyEntity> implements HostCompanyService {

    @Autowired
    private HostCompanyDao hostCompanyDao;
    @Autowired
    RedissonClient redisson;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public IPage<HostCompanyEntity> page(Map<String, Object> params) {
        IPage<HostCompanyEntity> page = this.page(
                new Query<HostCompanyEntity>().getPage(params),
                new QueryWrapper<HostCompanyEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public List<HostCompanyEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<HostCompanyEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<HostCompanyEntity> queryByHostList(List<String> hostList) {
        return hostCompanyDao.queryByHostList(hostList);
    }

    @Override
    public HostCompanyEntity queryByHost(String host) {
        return hostCompanyDao.queryByHost(host);
    }

    @Override
    public void updatePorts(String domain, String scanPorts) {
//        String parentDomain = RexpUtil.getMajorDomain(domain);
        String lockKey = String.format(CacheConst.REDIS_LOCK_HOST_INFO, domain);
        RLock lock = redisson.getLock(lockKey);
        try {
            lock.lock();
            HostCompanyEntity hostInfo = hostCompanyDao.queryByHost(domain);
            Date now = new Date();
            String ports = PortUtils.getAllPorts(scanPorts, hostInfo.getScanPorts());
            hostInfo.setScanPorts(ports);
            hostInfo.setScanTime(now);
            updateById(hostInfo);

            HostCompanyEntity obj = HostCompanyEntity.builder()
                    .host(domain).company(hostInfo.getCompany())
                    .scanPorts(ports).scanTime(now)
                    .build();
            stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_HOST_INFO, domain), JSON.toJSONString(obj));
        } catch (Exception e) {
            log.error(domain + "主域名信息更新报错", e);
        } finally {
            // 判断当前线程是否持有锁
            if (lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
    }

    @Override
    public String getCompany(String domain) {
//        String parentDomain = RexpUtil.getMajorDomain(domain);
        String value = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_HOST_INFO, domain));
        if (!StringUtils.isEmpty(value)) {
            HostCompanyEntity hostInfo = JSON.parseObject(value, HostCompanyEntity.class);
            return StringUtils.isEmpty(hostInfo.getCompany()) ? Const.STR_EMPTY : hostInfo.getCompany();
        }
        return Const.STR_CROSSBAR;
    }

    @Override
    public String getScanPorts(String domain) {
//        String parentDomain = RexpUtil.getMajorDomain(domain);
        String value = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_HOST_INFO, domain));
        if (!StringUtils.isEmpty(value)) {
            HostCompanyEntity hostInfo = JSON.parseObject(value, HostCompanyEntity.class);
            return StringUtils.isEmpty(hostInfo.getScanPorts()) ? Const.STR_EMPTY : hostInfo.getScanPorts();
        }
        return Const.STR_EMPTY;
    }

    @Override
    public HostCompanyEntity getHostInfo(String domain) {
//        String parentDomain = RexpUtil.getMajorDomain(domain);
        HostCompanyEntity hostInfo;
        String value = stringRedisTemplate.opsForValue().get(String.format(CacheConst.REDIS_HOST_INFO, domain));
        if (!StringUtils.isEmpty(value)) {
            hostInfo = JSON.parseObject(value, HostCompanyEntity.class);
        } else {
            hostInfo = HostCompanyEntity.builder()
                    .company(Const.STR_CROSSBAR).scanPorts(Const.STR_CROSSBAR)
                    .build();
        }
        return hostInfo;
    }

    @Override
    public HostCompanyEntity setHostInfo(String domain) {
//        String parentDomain = RexpUtil.getMajorDomain(domain);
        HostCompanyEntity hostInfo = hostCompanyDao.queryByHost(domain);
        String company = hostInfo == null ? Const.STR_CROSSBAR : StringUtils.isEmpty(hostInfo.getCompany()) ? Const.STR_CROSSBAR : hostInfo.getCompany();
        String ports = hostInfo == null ? Const.STR_CROSSBAR : StringUtils.isEmpty(hostInfo.getScanPorts()) ? Const.STR_CROSSBAR : hostInfo.getScanPorts();
        HostCompanyEntity obj = HostCompanyEntity.builder()
                .host(domain).company(company).scanPorts(ports)
                .build();
        stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_HOST_INFO, domain), JSON.toJSONString(obj));
        return obj;
    }

    @Override
    public List<HostCompanyEntity> setHostInfoList(List<String> domainList) {
        List<HostCompanyEntity> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(domainList)) {
            return result;
        }
        //        String parentDomain = RexpUtil.getMajorDomain(domain);
        List<HostCompanyEntity> hostInfoList = hostCompanyDao.queryByHostList(domainList);
        if (!CollectionUtils.isEmpty(hostInfoList)) {
            for (HostCompanyEntity hostInfo : hostInfoList) {
                String company = hostInfo == null ? Const.STR_CROSSBAR : StringUtils.isEmpty(hostInfo.getCompany()) ? Const.STR_CROSSBAR : hostInfo.getCompany();
                String ports = hostInfo == null ? Const.STR_CROSSBAR : StringUtils.isEmpty(hostInfo.getScanPorts()) ? Const.STR_CROSSBAR : hostInfo.getScanPorts();
                HostCompanyEntity obj = HostCompanyEntity.builder()
                        .host(hostInfo.getHost()).company(company).scanPorts(ports)
                        .build();
                result.add(obj);
                stringRedisTemplate.opsForValue().set(String.format(CacheConst.REDIS_HOST_INFO, hostInfo.getHost()), JSON.toJSONString(obj));
            }
        }
        return result;
    }

}
