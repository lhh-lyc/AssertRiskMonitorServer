package com.lhh.serverscanhole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.NetErrorDataEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverscanhole.dao.NetErrorDataDao;
import com.lhh.serverscanhole.service.NetErrorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("netErrorDataService")
public class NetErrorDataServiceImpl extends ServiceImpl<NetErrorDataDao, NetErrorDataEntity> implements NetErrorDataService {

    @Autowired
    private NetErrorDataDao netErrorDataDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<NetErrorDataEntity> page(Map<String, Object> params) {
        IPage<NetErrorDataEntity> page = this.page(
                new Query<NetErrorDataEntity>().getPage(params),
                new QueryWrapper<NetErrorDataEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<NetErrorDataEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<NetErrorDataEntity> list = list(wrapper);
        return list;
    }

}
