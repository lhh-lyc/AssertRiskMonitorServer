package com.lhh.serveradmin.controller.sys;

import com.lhh.serveradmin.service.sys.SysLetterService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.SysLetterEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;



/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-06-11 12:18:45
 */
@RestController
@RequestMapping("sys/letter")
public class SysLetterController {
    @Autowired
    private SysLetterService sysLetterService;

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public R page(@RequestParam Map<String, Object> params) {
        return sysLetterService.page(params);
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public R list(@RequestParam Map<String, Object> params) {
        return sysLetterService.list(params);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    @ApiOperation(value = "根据表格字段查询列表")
    public R listM(@RequestParam Map<String, Object> params) {
        return sysLetterService.listByMap(params);
    }

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public R save(@RequestBody SysLetterEntity sysLetter) {
        return sysLetterService.save(sysLetter);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public R save(@RequestBody List<SysLetterEntity> sysLetterList) {
        return sysLetterService.saveBatch(sysLetterList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public R update(@RequestBody SysLetterEntity sysLetter) {
        return sysLetterService.update(sysLetter);
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @PostMapping("delete")
    @ApiOperation(value = "单个删除")
    public R delete(Long id) {
        return sysLetterService.delete(id);
    }

    /**
     * 批量删除
     * @return
     */
    @PostMapping("deleteBatch")
    @ApiOperation(value = "批量删除")
    public R deleteBatch(@RequestBody Long[] ids) {
        return sysLetterService.deleteBatch(ids);
    }

    /**
    * 详情
    * @param id
    * @return
    */
    @GetMapping("info")
    @ApiOperation(value = "详情")
    public R getInfo(Long id) {
        return sysLetterService.info(id);
    }

    @PostMapping("/read")
    @ApiOperation(value = "read")
    public R read(@RequestBody Map<String, Object> params) {
        return sysLetterService.read(params);
    }

    @GetMapping("unReadNum")
    @ApiOperation(value = "详情")
    public R unReadNum() {
        return sysLetterService.unReadNum();
    }

}