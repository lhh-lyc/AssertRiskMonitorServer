package com.lhh.serverinfocommon.service.imsp.scan;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.ScanHostPortEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.scan.ScanHostPortDao;
import com.lhh.serverinfocommon.service.scan.ScanHostPortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("scanHostPortService")
public class ScanHostPortServiceImpl extends ServiceImpl<ScanHostPortDao, ScanHostPortEntity> implements ScanHostPortService {

    @Autowired
    private ScanHostPortDao scanHostPortDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<ScanHostPortEntity> page(Map<String, Object> params) {
        IPage<ScanHostPortEntity> page = this.page(
                new Query<ScanHostPortEntity>().getPage(params),
                new QueryWrapper<ScanHostPortEntity>()
        );
        return page;
    }

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<ScanHostPortEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<ScanHostPortEntity> list = list(wrapper);
        return list;
    }

}
