package com.lhh.serverscanhole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.ScanSecurityHoleTaskEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverscanhole.dao.ScanSecurityHoleTaskDao;
import com.lhh.serverscanhole.service.ScanSecurityHoleTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("scanSecurityHoleTaskService")
public class ScanSecurityHoleTaskServiceImpl extends ServiceImpl<ScanSecurityHoleTaskDao, ScanSecurityHoleTaskEntity> implements ScanSecurityHoleTaskService {

    @Autowired
    private ScanSecurityHoleTaskDao scanSecurityHoleTaskDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanSecurityHoleTaskEntity> page(Map<String, Object> params) {
        IPage<ScanSecurityHoleTaskEntity> page = this.page(
                new Query<ScanSecurityHoleTaskEntity>().getPage(params),
                new QueryWrapper<ScanSecurityHoleTaskEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanSecurityHoleTaskEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<ScanSecurityHoleTaskEntity> list = list(wrapper);
        return list;
    }

}
