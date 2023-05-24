package com.lhh.serverinfocommon.service.imsp.scan;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.scan.ScanProjectContentDao;
import com.lhh.serverinfocommon.service.scan.ScanProjectContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("scanProjectContentService")
public class ScanProjectContentServiceImpl extends ServiceImpl<ScanProjectContentDao, ScanProjectContentEntity> implements ScanProjectContentService {

    @Autowired
    private ScanProjectContentDao scanProjectContentDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanProjectContentEntity> page(Map<String, Object> params) {
        IPage<ScanProjectContentEntity> page = this.page(
                new Query<ScanProjectContentEntity>().getPage(params),
                new QueryWrapper<ScanProjectContentEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanProjectContentEntity> list(Map<String, Object> params) {
        List<ScanProjectContentEntity> list = scanProjectContentDao.queryList(params);
        return list;
    }

    @Override
    public List<ScanProjectContentEntity> getContentIpList(List<String> notIdList) {
        return scanProjectContentDao.getContentIpList(notIdList);
    }

    @Override
    public void updateEndScanContent() {
        scanProjectContentDao.updateEndScanContent();
    }

}
