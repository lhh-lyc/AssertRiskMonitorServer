package com.lhh.serverinfocommon.service.imsp.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.request.PageUtil;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.entity.SysLetterEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.sys.SysLetterDao;
import com.lhh.serverinfocommon.service.sys.SysLetterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("sysLetterService")
public class SysLetterServiceImpl extends ServiceImpl<SysLetterDao, SysLetterEntity> implements SysLetterService {

    @Autowired
    private SysLetterDao sysLetterDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<SysLetterEntity> page(Map<String, Object> params) {
        Page<SysLetterEntity> page = PageUtil.getPageParam(params);
        return sysLetterDao.queryPage(page, params);
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<SysLetterEntity> list(Map<String, Object> params) {
        List<SysLetterEntity> list = sysLetterDao.queryList(params);
        return list;
    }

    @Override
    public void readByUserId(Long userId) {
        sysLetterDao.readByUserId(userId);
    }

    @Override
    public Integer unReadNum(Long userId) {
        return sysLetterDao.unReadNum(userId);
    }

}
