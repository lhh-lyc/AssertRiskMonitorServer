package com.lhh.serverscanhole.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.HoleYamlEntity;
import com.lhh.serverscanhole.dao.HoleYamlDao;
import com.lhh.serverscanhole.service.HoleYamlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("HoleYamlService")
public class HoleYamlServiceImpl extends ServiceImpl<HoleYamlDao, HoleYamlEntity> implements HoleYamlService {

    @Autowired
    private HoleYamlDao holeYamlDao;

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<HoleYamlEntity> list(Map<String, Object> params) {
        return holeYamlDao.queryList(params);
    }

    @Override
    public List<HoleYamlEntity> delList(Map<String, Object> params) {
        return holeYamlDao.queryDelList(params);
    }

}
