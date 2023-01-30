package com.lhh.serverinfocommon.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.SysFilesEntity;
import com.lhh.serverinfocommon.service.SysFilesService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 系统_文件表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@RestController
@RequestMapping("sysfiles")
public class SysFilesController {
    @Autowired
    private SysFilesService sysFilesService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public void save(@RequestBody SysFilesEntity sysFiles) {
            sysFilesService.save(sysFiles);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public void save(@RequestBody List<SysFilesEntity> sysFilesList) {
            sysFilesService.saveBatch(sysFilesList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public void update(@RequestBody SysFilesEntity sysFiles) {
            sysFilesService.updateById(sysFiles);
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
                sysFilesService.removeById(id);
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
            sysFilesService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    @ApiOperation(value = "根据表格字段查询列表")
    public List<SysFilesEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<SysFilesEntity> sysFilesList = (List<SysFilesEntity>) sysFilesService.listByMap(params);
        return sysFilesList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public IPage<SysFilesEntity> page(@RequestParam Map<String, Object> params) {
        IPage<SysFilesEntity> page = sysFilesService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public List<SysFilesEntity> list(@RequestParam Map<String, Object> params) {
        List<SysFilesEntity> sysFilesList = sysFilesService.list(params);
        return sysFilesList;
    }

    @GetMapping("info")
    @ApiOperation(value = "详情")
    public SysFilesEntity getInfo(Long id) {
            SysFilesEntity sysFiles = sysFilesService.getById(id);
        return sysFiles;
    }

}

