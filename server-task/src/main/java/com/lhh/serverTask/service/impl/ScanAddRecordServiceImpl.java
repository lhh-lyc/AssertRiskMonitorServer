package com.lhh.serverTask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.ScanAddRecordEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverTask.dao.ScanAddRecordDao;
import com.lhh.serverTask.service.ScanAddRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("scanAddRecordService")
public class ScanAddRecordServiceImpl extends ServiceImpl<ScanAddRecordDao, ScanAddRecordEntity> implements ScanAddRecordService {

    @Autowired
    private ScanAddRecordDao scanAddRecordDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanAddRecordEntity> page(Map<String, Object> params) {
        IPage<ScanAddRecordEntity> page = this.page(
                new Query<ScanAddRecordEntity>().getPage(params),
                new QueryWrapper<ScanAddRecordEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanAddRecordEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<ScanAddRecordEntity> list = list(wrapper);
        return list;
    }

}
