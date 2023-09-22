package com.lhh.serveradmin.controller.scan;

import com.lhh.serveradmin.service.scan.ScanSecurityHoleService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;



/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-09-12 15:41:27
 */
@RestController
@RequestMapping("scan/security/hole")
public class ScanSecurityHoleController {
    @Autowired
    private ScanSecurityHoleService scanSecurityHoleService;

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public R page(@RequestParam Map<String, Object> params) {
        return scanSecurityHoleService.page(params);
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public R list(@RequestParam Map<String, Object> params) {
        return scanSecurityHoleService.list(params);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    @ApiOperation(value = "根据表格字段查询列表")
    public R listM(@RequestParam Map<String, Object> params) {
        return scanSecurityHoleService.listByMap(params);
    }

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public R save(@RequestBody ScanSecurityHoleEntity scanSecurityHole) {
        return scanSecurityHoleService.save(scanSecurityHole);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public R save(@RequestBody List<ScanSecurityHoleEntity> scanSecurityHoleList) {
        return scanSecurityHoleService.saveBatch(scanSecurityHoleList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public R update(@RequestBody ScanSecurityHoleEntity scanSecurityHole) {
        return scanSecurityHoleService.update(scanSecurityHole);
    }

    /**
     * 单个删除
     * @param ids
     * @return
     */
    @PostMapping("delete")
    @ApiOperation(value = "删除")
    public R delete(@RequestBody List<Long> ids) {
        return scanSecurityHoleService.delete(ids);
    }

    /**
    * 详情
    * @param id
    * @return
    */
    @GetMapping("info")
    @ApiOperation(value = "详情")
    public R getInfo(Long id) {
        return scanSecurityHoleService.info(id);
    }

}