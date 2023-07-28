package com.lhh.serverinfocommon.service.imsp.scan;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.scan.HostCompanyDao;
import com.lhh.serverinfocommon.service.scan.HostCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("hostCompanyService")
public class HostCompanyServiceImpl extends ServiceImpl<HostCompanyDao, HostCompanyEntity> implements HostCompanyService {

    @Autowired
    private HostCompanyDao hostCompanyDao;

    /**
     * 分页查询列表数据
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
    public HostCompanyEntity queryBasicInfo(String host) {
        return hostCompanyDao.queryBasicInfo(host);
    }

}
