package com.lhh.serverinfocommon.controller.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.SysDictEntity;
import com.lhh.serverinfocommon.service.sys.SysDictService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 系统_字典表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@RestController
@RequestMapping("sys/dict")
public class SysDictController {
    @Autowired
    private SysDictService sysDictService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public void save(@RequestBody SysDictEntity sysDict) {
            sysDictService.save(sysDict);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public void save(@RequestBody List<SysDictEntity> sysDictList) {
            sysDictService.saveBatch(sysDictList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public void update(@RequestBody SysDictEntity sysDict) {
            sysDictService.updateById(sysDict);
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @PostMapping("delete")
    @ApiOperation(value = "单个删除")
    public void delete(Long id) {
        if (id != null) {
                sysDictService.removeById(id);
        }
    }

    /**
     * 批量删除
     * @return
     */
    @PostMapping("deleteBatch")
    @ApiOperation(value = "批量删除")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
            sysDictService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    @ApiOperation(value = "根据表格字段查询列表")
    public List<SysDictEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<SysDictEntity> sysDictList = (List<SysDictEntity>) sysDictService.listByMap(params);
        return sysDictList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public IPage<SysDictEntity> page(@RequestParam Map<String, Object> params) {
        IPage<SysDictEntity> page = sysDictService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public List<SysDictEntity> list(@RequestParam Map<String, Object> params) {
        List<SysDictEntity> sysDictList = sysDictService.list(params);
        return sysDictList;
    }

    @GetMapping("info")
    @ApiOperation(value = "详情")
    public SysDictEntity getInfo(Long id) {
            SysDictEntity sysDict = sysDictService.getById(id);
        return sysDict;
    }

}

