package com.lhh.serverinfocommon.service.imsp.scan;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.interfaces.Compare;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.KeyValueDto;
import com.lhh.serverbase.dto.ScanResultDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.scan.ScanHostDao;
import com.lhh.serverinfocommon.service.scan.ScanHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;


@Service("scanHostService")
public class ScanHostServiceImpl extends ServiceImpl<ScanHostDao, ScanHostEntity> implements ScanHostService {

    @Autowired
    private ScanHostDao scanHostDao;

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
        return list;
    }

}
