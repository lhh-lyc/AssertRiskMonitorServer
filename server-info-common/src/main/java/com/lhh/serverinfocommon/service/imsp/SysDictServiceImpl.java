package com.lhh.serverinfocommon.service.imsp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.SysDictEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.SysDictDao;
import com.lhh.serverinfocommon.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("sysDictService")
public class SysDictServiceImpl extends ServiceImpl<SysDictDao, SysDictEntity> implements SysDictService {

    @Autowired
    private SysDictDao sysDictDao;

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<SysDictEntity> page(Map<String, Object> params) {
        IPage<SysDictEntity> page = this.page(
                new Query<SysDictEntity>().getPage(params),
                new QueryWrapper<SysDictEntity>()
        );
        return page;
    }

    /**
     * 查询用户列表
     * @param params
     * @return
     */
    @Override
    public List<SysDictEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        List<SysDictEntity> list = list(wrapper);
        return list;
    }

}
