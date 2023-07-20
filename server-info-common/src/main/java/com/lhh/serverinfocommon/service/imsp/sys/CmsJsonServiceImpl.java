package com.lhh.serverinfocommon.service.imsp.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.CmsJsonEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.sys.CmsJsonDao;
import com.lhh.serverinfocommon.service.sys.CmsJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("cmsJsonService")
public class CmsJsonServiceImpl extends ServiceImpl<CmsJsonDao, CmsJsonEntity> implements CmsJsonService {

    @Autowired
    private CmsJsonDao cmsJsonDao;

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public IPage<CmsJsonEntity> page(Map<String, Object> params) {
        IPage<CmsJsonEntity> page = this.page(
                new Query<CmsJsonEntity>().getPage(params),
                new QueryWrapper<CmsJsonEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     *
     * @param params
     * @return
     */
    @Override
    public List<CmsJsonEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<CmsJsonEntity> list = list(wrapper);
        return list;
    }

    @Override
    public void clearAll() {
        cmsJsonDao.clearAll();
    }

}
