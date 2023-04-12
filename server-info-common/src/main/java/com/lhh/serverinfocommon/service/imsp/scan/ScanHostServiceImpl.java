package com.lhh.serverinfocommon.service.imsp.scan;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.dto.ScanResultDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.scan.ScanHostDao;
import com.lhh.serverinfocommon.service.scan.ScanHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
                .in("domain", hostList);
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanHostEntity> getByIpList(List<String> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return new ArrayList<>();
        }
        QueryWrapper wrapper = Wrappers.query()
                .in("ip", hostList);
        List<ScanHostEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanResultDto> queryDomainGroupList(Map<String, Object> params) {
        return scanHostDao.queryDomainGroupList(params);
    }

}
