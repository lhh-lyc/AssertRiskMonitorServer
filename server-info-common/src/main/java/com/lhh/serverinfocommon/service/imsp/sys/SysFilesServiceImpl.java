package com.lhh.serverinfocommon.service.imsp.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.request.PageUtil;
import com.lhh.serverbase.entity.SysFilesEntity;
import com.lhh.serverinfocommon.dao.sys.SysFilesDao;
import com.lhh.serverinfocommon.service.sys.SysFilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("sysFilesService")
public class SysFilesServiceImpl extends ServiceImpl<SysFilesDao, SysFilesEntity> implements SysFilesService {

    @Autowired
    private SysFilesDao sysFilesDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<SysFilesEntity> page(Map<String, Object> params) {
        Page<SysFilesEntity> page = PageUtil.getPageParam(params);
        return sysFilesDao.queryPage(page, params);
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<SysFilesEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<SysFilesEntity> list = list(wrapper);
        return list;
    }

}
