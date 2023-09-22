package com.lhh.serveradmin.service.scan;

import com.lhh.serveradmin.feign.scan.ScanSecurityHoleFeign;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


/**
 * 系统_用户表应用服务层
 *
 * @author lyc
 * @date 2023-09-12 15:41:27
 */
@Service
public class ScanSecurityHoleService {

    @Autowired
    private ScanSecurityHoleFeign scanSecurityHoleFeign;

    /**
     * 查询 分页数据
     *
     * @param
     */
    public R page(Map<String, Object> params) {
        IPage<ScanSecurityHoleEntity> page = scanSecurityHoleFeign.page(params);
        return R.ok(page);
    }

    /**
     * 根据表格字段查询列表
     */
    public R listByMap(Map<String, Object> params) {
        List<ScanSecurityHoleEntity> list = scanSecurityHoleFeign.listByMap(params);
        return R.ok(list);
    }

    /**
     * 查询 列表数据
     *
     * @param
     */
    public R list(Map<String, Object> params) {
        List<ScanSecurityHoleEntity> list = scanSecurityHoleFeign.list(params);
        return R.ok(list);
    }

    /**
     * 保存
     */
    public R save(@RequestBody ScanSecurityHoleEntity scanSecurityHole) {
        scanSecurityHoleFeign.save(scanSecurityHole);
        return R.ok();
    }

    /**
     * 批量保存
     *
     * @return
     */
    public R saveBatch(@RequestBody List<ScanSecurityHoleEntity> ScanSecurityHoleList) {
        scanSecurityHoleFeign.saveBatch(ScanSecurityHoleList);
        return R.ok();
    }

    /**
     * 更新
     *
     * @return
     */
    public R update(@RequestBody ScanSecurityHoleEntity scanSecurityHole) {
        scanSecurityHoleFeign.update(scanSecurityHole);
        return R.ok();
    }

    /**
     * 批量逻辑删除
     *
     * @return
     */
    public R delete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return R.error("请选择删除数据");
        }
        scanSecurityHoleFeign.deleteBatch(ids);
        return R.ok();
    }

    /**
     * 查询详情
     *
     * @return
     */
    public R info(@RequestParam(name = "id") Long id) {
        ScanSecurityHoleEntity scanSecurityHole = scanSecurityHoleFeign.info(id);
        return R.ok(scanSecurityHole);
    }

}



