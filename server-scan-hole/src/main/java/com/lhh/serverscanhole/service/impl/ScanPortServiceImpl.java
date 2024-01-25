package com.lhh.serverscanhole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverscanhole.dao.ScanPortDao;
import com.lhh.serverscanhole.service.ScanPortService;
import com.lhh.serverscanhole.utils.JedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Service("scanPortService")
public class ScanPortServiceImpl extends ServiceImpl<ScanPortDao, ScanPortEntity> implements ScanPortService {

    @Autowired
    private ScanPortDao scanPortDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanPortEntity> page(Map<String, Object> params) {
        IPage<ScanPortEntity> page = this.page(
                new Query<ScanPortEntity>().getPage(params),
                new QueryWrapper<ScanPortEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanPortEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query()
                .eq("del_flg", Const.INTEGER_0)
                .eq(params.get("ipLong") != null, "ip_long", params.get("ipLong"));
        List<ScanPortEntity> list = list(wrapper);
        return list;
    }

    @Override
    public List<Integer> queryWebPortList(String domain) {
        String serverNames = JedisUtils.getJson(CacheConst.REDIS_SERVER_NAMES);
        if (StringUtils.isEmpty(serverNames)) {
            JedisUtils.setJson(CacheConst.REDIS_SERVER_NAMES, "http,https");
        }
        List<String> serverNameList = Arrays.asList(serverNames.split(Const.STR_COMMA));
        return scanPortDao.queryWebPortList(domain, serverNameList);
    }

    @Override
    public List<Integer> queryPortList(String domain) {
        return scanPortDao.queryPortList(domain);
    }

    @Override
    public List<ScanPortEntity> basicList(Map<String, Object> params) {
        List<ScanPortEntity> list = scanPortDao.queryList(params);
        return list;
    }

    @Override
    public List<ScanPortEntity> basicByIpList(List<Long> ipList) {
        return scanPortDao.basicByIpList(ipList);
    }

    @Override
    public void saveBatch(List<ScanPortEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        scanPortDao.saveBatch(list);
    }

    @Override
    public void updateBatch(List<ScanPortEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        scanPortDao.updateBatch(list);
    }

    @Override
    public void delBatch(List<Long> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        scanPortDao.delBatch(list);
    }

}
