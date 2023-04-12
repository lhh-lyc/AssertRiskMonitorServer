package com.lhh.serverinfocommon.controller.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.SysLoginLogEntity;
import com.lhh.serverinfocommon.service.sys.SysLoginLogService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 系统_登录日志表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@RestController
@RequestMapping("sysloginlog")
public class SysLoginLogController {
    @Autowired
    private SysLoginLogService sysLoginLogService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public void save(@RequestBody SysLoginLogEntity sysLoginLog) {
            sysLoginLogService.save(sysLoginLog);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public void save(@RequestBody List<SysLoginLogEntity> sysLoginLogList) {
            sysLoginLogService.saveBatch(sysLoginLogList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public void update(@RequestBody SysLoginLogEntity sysLoginLog) {
            sysLoginLogService.updateById(sysLoginLog);
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
                sysLoginLogService.removeById(id);
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
            sysLoginLogService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    @ApiOperation(value = "根据表格字段查询列表")
    public List<SysLoginLogEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<SysLoginLogEntity> sysLoginLogList = (List<SysLoginLogEntity>) sysLoginLogService.listByMap(params);
        return sysLoginLogList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public IPage<SysLoginLogEntity> page(@RequestParam Map<String, Object> params) {
        IPage<SysLoginLogEntity> page = sysLoginLogService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public List<SysLoginLogEntity> list(@RequestParam Map<String, Object> params) {
        List<SysLoginLogEntity> sysLoginLogList = sysLoginLogService.list(params);
        return sysLoginLogList;
    }

    @GetMapping("info")
    @ApiOperation(value = "详情")
    public SysLoginLogEntity getInfo(Long id) {
            SysLoginLogEntity sysLoginLog = sysLoginLogService.getById(id);
        return sysLoginLog;
    }

}

