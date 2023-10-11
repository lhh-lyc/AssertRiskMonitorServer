package com.lhh.serverinfocommon.service.imsp.scan;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.request.PageUtil;
import com.lhh.serverbase.dto.HoleNumDto;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.vo.ScanHoleVo;
import com.lhh.serverbase.vo.ScanPortVo;
import com.lhh.serverinfocommon.dao.scan.ScanSecurityHoleDao;
import com.lhh.serverinfocommon.service.scan.ScanSecurityHoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("scanSecurityHoleService")
public class ScanSecurityHoleServiceImpl extends ServiceImpl<ScanSecurityHoleDao, ScanSecurityHoleEntity> implements ScanSecurityHoleService {

    @Autowired
    private ScanSecurityHoleDao scanSecurityHoleDao;

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public IPage<ScanSecurityHoleEntity> page(Map<String, Object> params) {
        Page<ScanSecurityHoleEntity> page = PageUtil.getPageParam(params);
        return scanSecurityHoleDao.queryPage(page, params);
    }

    @Override
    public List<ScanHoleVo> exportList(Map<String, Object> params) {
        Page<ScanHoleVo> page = PageUtil.getPageParam(params);
        IPage<ScanHoleVo> p = scanSecurityHoleDao.exportList(page, params);
        return p.getRecords();
    }

    @Override
    public Integer exportNum(Map<String, Object> params) {
        return scanSecurityHoleDao.exportNum(params);
    }

    /**
     * 查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public List<ScanSecurityHoleEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<ScanSecurityHoleEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanSecurityHoleEntity> basicList(Map<String, Object> params) {
        return scanSecurityHoleDao.basicList(params);
    }

    @Override
    public HomeNumDto queryHomeNum(Map<String, Object> params) {
        return scanSecurityHoleDao.queryHomeNum(params);
    }

    @Override
    public List<HoleNumDto> queryHoleNum(Map<String, Object> params) {
        return scanSecurityHoleDao.queryHoleNum(params);
    }

}
