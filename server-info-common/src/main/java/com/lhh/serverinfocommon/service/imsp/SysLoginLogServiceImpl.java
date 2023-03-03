package com.lhh.serverinfocommon.service.imsp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.SysLoginLogEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.SysLoginLogDao;
import com.lhh.serverinfocommon.service.SysLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("sysLoginLogService")
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogDao, SysLoginLogEntity> implements SysLoginLogService {

    @Autowired
    private SysLoginLogDao sysLoginLogDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<SysLoginLogEntity> page(Map<String, Object> params) {
        IPage<SysLoginLogEntity> page = this.page(
                new Query<SysLoginLogEntity>().getPage(params),
                new QueryWrapper<SysLoginLogEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<SysLoginLogEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<SysLoginLogEntity> list = list(wrapper);
        return list;
    }

}
