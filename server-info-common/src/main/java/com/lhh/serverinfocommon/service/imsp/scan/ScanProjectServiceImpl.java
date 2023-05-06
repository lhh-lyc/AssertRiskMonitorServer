package com.lhh.serverinfocommon.service.imsp.scan;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.PageUtil;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.serverinfocommon.dao.scan.ScanProjectDao;
import com.lhh.serverinfocommon.service.scan.ScanHostService;
import com.lhh.serverinfocommon.service.scan.ScanProjectHostService;
import com.lhh.serverinfocommon.service.scan.ScanProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("scanProjectService")
public class ScanProjectServiceImpl extends ServiceImpl<ScanProjectDao, ScanProjectEntity> implements ScanProjectService {

    @Autowired
    private ScanProjectDao scanProjectDao;
    @Autowired
    ScanHostService scanHostService;
    @Autowired
    ScanProjectHostService scanProjectHostService;

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public IPage<ScanProjectEntity> page(Map<String, Object> params) {
        Page<ScanProjectEntity> page = PageUtil.getPageParam(params);
        return scanProjectDao.queryPage(page, params);
    }

    /**
     * 查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public List<ScanProjectEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query()
                .eq(params.get("userId") != null, "user_id", params.get("userId"))
                .eq(params.get("name") != null, "name", params.get("name"))
                .eq("del_flg", Const.INTEGER_0);
        List<ScanProjectEntity> list = list(wrapper);
        return list;
    }

}
