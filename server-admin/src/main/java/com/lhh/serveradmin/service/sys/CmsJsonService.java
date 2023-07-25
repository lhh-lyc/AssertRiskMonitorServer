package com.lhh.serveradmin.service.sys;

import com.lhh.serveradmin.feign.sys.CmsJsonFeign;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.CmsJsonEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


/**
 * 系统_用户表应用服务层
 *
 * @author lyc
 * @date 2023-07-18 15:08:23
 */
@Service
public class CmsJsonService {

    @Autowired
    private CmsJsonFeign cmsJsonFeign;

    /**
     * 查询 分页数据
     *
     * @param
     */
    public R page(Map<String, Object> params) {
        IPage<CmsJsonEntity> page = cmsJsonFeign.page(params);
        return R.ok(page);
    }

    /**
     * 查询 列表数据
     *
     * @param
     */
    public R list(Map<String, Object> params) {
        List<CmsJsonEntity> list = cmsJsonFeign.list(params);
        return R.ok(list);
    }

    /**
     * 保存
     */
    public R save(@RequestBody CmsJsonEntity cmsJson) {
        cmsJsonFeign.save(cmsJson);
        return R.ok();
    }

    /**
     * 批量保存
     *
     * @return
     */
    public R saveBatch(@RequestBody List<CmsJsonEntity> CmsJsonList) {
        cmsJsonFeign.saveBatch(CmsJsonList);
        return R.ok();
    }

    /**
     * 更新
     *
     * @return
     */
    public R update(@RequestBody CmsJsonEntity cmsJson) {
        cmsJsonFeign.update(cmsJson);
        return R.ok();
    }

    /**
     * 单个逻辑删除
     *
     * @param id
     * @return
     */
    public R delete(Long id) {
        cmsJsonFeign.delete(id);
        return R.ok();
    }

    /**
     * 批量逻辑删除
     *
     * @return
     */
    public R deleteBatch(Long[] ids) {
        cmsJsonFeign.deleteBatch(ids);
        return R.ok();
    }

    /**
     * 查询详情
     *
     * @return
     */
    public R info(@RequestParam(name = "id") Long id) {
        CmsJsonEntity cmsJson = cmsJsonFeign.info(id);
        return R.ok(cmsJson);
    }

}



