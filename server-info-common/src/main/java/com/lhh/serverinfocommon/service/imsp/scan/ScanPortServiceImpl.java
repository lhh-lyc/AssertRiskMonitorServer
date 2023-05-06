package com.lhh.serverinfocommon.service.imsp.scan;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.request.PageUtil;
import com.lhh.serverbase.dto.GroupTagDto;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverbase.vo.ScanPortVo;
import com.lhh.serverinfocommon.dao.scan.ScanPortDao;
import com.lhh.serverinfocommon.service.scan.ScanPortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service("scanPortService")
public class ScanPortServiceImpl extends ServiceImpl<ScanPortDao, ScanPortEntity> implements ScanPortService {

    @Autowired
    private ScanPortDao scanPortDao;

    @Override
    public IPage<ScanPortEntity> page(Map<String, Object> params) {
        Page<ScanPortEntity> page = PageUtil.getPageParam(params);
        return scanPortDao.page(page, params);
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanPortEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<ScanPortEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<ScanPortEntity> getByIpList(List<String> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return new ArrayList<>();
        }
        QueryWrapper wrapper = Wrappers.query()
                .in("ip", hostList);
        List<ScanPortEntity> list = list(wrapper);
        return list;
    }

    @Override
    public HomeNumDto queryHomeNum(Map<String, Object> params) {
        return scanPortDao.queryHomeNum(params);
    }

    @Override
    public IPage<GroupTagDto> queryGroupTag(Map<String, Object> params) {
        Page<GroupTagDto> page = PageUtil.getPageParam(params);
        return scanPortDao.queryGroupTag(page, params);
    }

    @Override
    public Integer queryGroupTagNum(Map<String, Object> params) {
        return scanPortDao.queryGroupTagNum(params);
    }

    @Override
    public List<ScanPortVo> exportList(Map<String, Object> params) {
        return scanPortDao.exportList(params);
    }

    @Override
    public void deleteByIpPort(Map<String, Object> params) {
        scanPortDao.deleteByIpPort(params);
    }

}
