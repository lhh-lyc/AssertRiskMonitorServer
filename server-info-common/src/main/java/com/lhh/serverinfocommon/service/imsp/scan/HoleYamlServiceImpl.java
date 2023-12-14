package com.lhh.serverinfocommon.service.imsp.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.common.request.PageUtil;
import com.lhh.serverbase.entity.HoleYamlEntity;
import com.lhh.serverinfocommon.dao.scan.HoleYamlDao;
import com.lhh.serverinfocommon.service.scan.HoleYamlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("HoleYamlService")
public class HoleYamlServiceImpl extends ServiceImpl<HoleYamlDao, HoleYamlEntity> implements HoleYamlService {

    @Autowired
    private HoleYamlDao holeYamlDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<HoleYamlEntity> page(Map<String, Object> params) {
        Page<HoleYamlEntity> page = PageUtil.getPageParam(params);
        return holeYamlDao.queryList(page, params);
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<HoleYamlEntity> list(Map<String, Object> params) {
        return holeYamlDao.queryList(params);
    }

}
