package com.lhh.serverinfocommon.controller.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.SysLogEntity;
import com.lhh.serverinfocommon.service.sys.SysLogService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 系统_日志表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@RestController
@RequestMapping("syslog")
public class SysLogController {
    @Autowired
    private SysLogService sysLogService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public void save(@RequestBody SysLogEntity sysLog) {
            sysLogService.save(sysLog);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public void save(@RequestBody List<SysLogEntity> sysLogList) {
            sysLogService.saveBatch(sysLogList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public void update(@RequestBody SysLogEntity sysLog) {
            sysLogService.updateById(sysLog);
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
                sysLogService.removeById(id);
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
            sysLogService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    @ApiOperation(value = "根据表格字段查询列表")
    public List<SysLogEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<SysLogEntity> sysLogList = (List<SysLogEntity>) sysLogService.listByMap(params);
        return sysLogList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public IPage<SysLogEntity> page(@RequestParam Map<String, Object> params) {
        IPage<SysLogEntity> page = sysLogService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public List<SysLogEntity> list(@RequestParam Map<String, Object> params) {
        List<SysLogEntity> sysLogList = sysLogService.list(params);
        return sysLogList;
    }

    @GetMapping("info")
    @ApiOperation(value = "详情")
    public SysLogEntity getInfo(Long id) {
            SysLogEntity sysLog = sysLogService.getById(id);
        return sysLog;
    }

}

