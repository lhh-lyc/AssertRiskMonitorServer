package com.lhh.serverinfocommon.service.imsp.scan;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.interfaces.Compare;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.KeyValueDto;
import com.lhh.serverbase.dto.ScanResultDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.scan.ScanHostDao;
import com.lhh.serverinfocommon.service.scan.ScanHostService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("scanHostService")
public class ScanHostServiceImpl extends ServiceImpl<ScanHostDao, ScanHostEntity> implements ScanHostService {

    @Autowired
    private ScanHostDao scanHostDao;
    @Autowired
    RedissonClient redisson;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanHostEntity> page(Map<String, Object> params) {
        IPage<ScanHostEntity> page = this.page(
                new Query<ScanHostEntity>().getPage(params),
                new QueryWrapper<ScanHostEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanHostEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanHostEntity> getByDomainList(List<String> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return new ArrayList<>();
        }
        QueryWrapper wrapper = Wrappers.query()
                .eq("del_flg", Const.INTEGER_0)
                .in("domain", hostList);
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanHostEntity> getByIpList(List<Long> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return new ArrayList<>();
        }
        QueryWrapper wrapper = Wrappers.query()
                .eq("del_flg", Const.INTEGER_0)
                .in("ip_long", hostList);
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanResultDto> queryDomainGroupList(Map<String, Object> params) {
        return scanHostDao.queryDomainGroupList(params);
    }

    @Override
    public List<ScanHostEntity> equalParams(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query()
                .eq("del_flg", Const.INTEGER_0)
                .eq(params.get("domain") != null, "domain", params.get("domain"))
                .eq(params.get("parentDomain") != null, "parent_domain", params.get("parentDomain"))
                .eq(params.get("ipLong") != null, "ipLong", params.get("ipLong"));
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public Integer getCompanyNum(Map<String, Object> params) {
        return scanHostDao.getCompanyNum(params);
    }

    @Override
    public Integer getDomainNum(Map<String, Object> params) {
        return scanHostDao.getDomainNum(params);
    }

    @Override
    public Integer getSubDomainNum(Map<String, Object> params) {
        return scanHostDao.getSubDomainNum(params);
    }

    @Override
    public List<String> getParentDomainList(Map<String, Object> params) {
        return scanHostDao.getParentDomainList(params);
    }

    @Override
    public List<KeyValueDto> companyRanking(Map<String, Object> params) {
        Integer limit = params.get("limit") != null ? Const.INTEGER_10 : MapUtil.getInt(params, "limit");
        params.put("limit", limit);
        List<KeyValueDto> list = scanHostDao.companyRanking(params);
        if (list.size() < limit) {
            List<String> companyList = list.stream().map(KeyValueDto::getType).collect(Collectors.toList());
            companyList.add(Const.STR_CROSSBAR);
            Integer num = limit - list.size();
            List<String> cList = scanHostDao.getCompanyList(companyList, num);
            if (!CollectionUtils.isEmpty(cList)) {
                for (String c : cList) {
                    KeyValueDto dto = KeyValueDto.builder()
                            .type(c).value(Const.STR_0)
                            .build();
                    list.add(dto);
                }
            }
        }
        return list;
    }

    @Override
    public void endScanIp(Long ipLong, String scanPorts) {
        log.info("开始补充更新host表" + ipLong + "数据状态");
        String lockKey = String.format(CacheConst.REDIS_LOCK_IP_SCAN_CHANGE, ipLong);
        RLock lock = redisson.getLock(lockKey);
        boolean success = true;
        try {
            success = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (success) {
                scanHostDao.updateEndScanIp(ipLong, scanPorts);
            }
        } catch (Exception e) {
            log.error("补充更新host表" + ipLong + "扫描状态出错", e);
        } finally {
            // 判断当前线程是否持有锁
            if (success && lock.isHeldByCurrentThread()) {
                //释放当前锁
                lock.unlock();
            }
        }
        log.info("补充更新结束host表" + ipLong + "数据状态");
    }

    @Override
    public List<String> getParentList() {
        return scanHostDao.getParentList();
    }

}
