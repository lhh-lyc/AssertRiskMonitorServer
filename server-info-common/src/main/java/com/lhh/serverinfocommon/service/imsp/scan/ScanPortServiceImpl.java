package com.lhh.serverinfocommon.service.imsp.scan;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.PageUtil;
import com.lhh.serverbase.dto.GroupTagDto;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverbase.vo.ScanPortVo;
import com.lhh.serverinfocommon.dao.scan.ScanHostDao;
import com.lhh.serverinfocommon.dao.scan.ScanPortDao;
import com.lhh.serverinfocommon.dao.scan.ScanProjectHostDao;
import com.lhh.serverinfocommon.service.scan.ScanPortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Service("scanPortService")
public class ScanPortServiceImpl extends ServiceImpl<ScanPortDao, ScanPortEntity> implements ScanPortService {

    @Autowired
    private ScanPortDao scanPortDao;
    @Autowired
    private ScanHostDao scanHostDao;
    @Autowired
    private ScanProjectHostDao scanProjectHostDao;

    @Override
    public IPage<ScanPortEntity> page(Map<String, Object> params) {
        Page<ScanPortEntity> page = PageUtil.getPageParam(params);
        if (params.get("ip") != null && !StringUtils.isEmpty(MapUtil.getStr(params, "ip"))) {
            Long ipLong = IpLongUtils.ipToLong(MapUtil.getStr(params, "ip"));
            params.put("ipLong", ipLong);
        }
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
    public List<ScanPortEntity> getByIpList(List<Long> hostList) {
        if (CollectionUtils.isEmpty(hostList)) {
            return new ArrayList<>();
        }
        QueryWrapper wrapper = Wrappers.query()
                .in("ip_long", hostList);
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
        Page<ScanPortVo> page = PageUtil.getPageParam(params);
        if (params.get("ip") != null && !StringUtils.isEmpty(MapUtil.getStr(params, "ip"))) {
            Long ipLong = IpLongUtils.ipToLong(MapUtil.getStr(params, "ip"));
            params.put("ipLong", ipLong);
        }
        IPage<ScanPortVo> p = scanPortDao.exportList(page, params);
        return p.getRecords();
    }

    @Override
    public Integer exportNum(Map<String, Object> params) {
        return scanPortDao.exportNum(params);
    }

    @Override
    public void deleteByIpPort(Map<String, Object> params) {
        scanPortDao.deleteByIpPort(params);
    }

    @Override
    public void deleteByTag(Map<String, Object> params) {
        Integer tagType = MapUtil.getInt(params, "tagType");
        List<String> selectList = (List<String>) params.get("selectList");
        Long userId = MapUtil.getLong(params, "userId");
        if (userId != null) {
            switch (tagType) {
                case 2:
                    scanProjectHostDao.deleteByTag(Arrays.asList("company"), selectList);
                    break;
                case 3:
                    scanProjectHostDao.deleteByTag(Arrays.asList("parent_domain"), selectList);
                    break;
                case 4:
                    scanProjectHostDao.deleteByTag(Arrays.asList("domain"), selectList);
                    break;
            }
        } else {
            switch (tagType) {
                case 2:
                    scanHostDao.deleteByTag(Arrays.asList("company"), selectList);
                    scanPortDao.deleteByTag(Arrays.asList("company"), selectList);
                    scanProjectHostDao.deleteByTag(Arrays.asList("company"), selectList);
                    break;
                case 3:
                    scanHostDao.deleteByTag(Arrays.asList("parent_domain"), selectList);
                    scanPortDao.deleteByTag(Arrays.asList("parent_domain"), selectList);
                    scanProjectHostDao.deleteByTag(Arrays.asList("parent_domain"), selectList);
                    break;
                case 4:
                    scanHostDao.deleteByTag(Arrays.asList("domain"), selectList);
                    scanPortDao.deleteByTag(Arrays.asList("domain"), selectList);
                    scanProjectHostDao.deleteByTag(Arrays.asList("domain"), selectList);
                    break;
            }
        }
    }

}
