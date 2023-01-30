package com.lhh.serverinfocommon.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.SysRoleEntity;
import com.lhh.serverinfocommon.service.SysRoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 系统_角色表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@RestController
@RequestMapping("sysrole")
public class SysRoleController {
    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public void save(@RequestBody SysRoleEntity sysRole) {
            sysRoleService.save(sysRole);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public void save(@RequestBody List<SysRoleEntity> sysRoleList) {
            sysRoleService.saveBatch(sysRoleList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public void update(@RequestBody SysRoleEntity sysRole) {
            sysRoleService.updateById(sysRole);
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
                sysRoleService.removeById(id);
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
            sysRoleService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    @ApiOperation(value = "根据表格字段查询列表")
    public List<SysRoleEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<SysRoleEntity> sysRoleList = (List<SysRoleEntity>) sysRoleService.listByMap(params);
        return sysRoleList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public IPage<SysRoleEntity> page(@RequestParam Map<String, Object> params) {
        IPage<SysRoleEntity> page = sysRoleService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public List<SysRoleEntity> list(@RequestParam Map<String, Object> params) {
        List<SysRoleEntity> sysRoleList = sysRoleService.list(params);
        return sysRoleList;
    }

    @GetMapping("info")
    @ApiOperation(value = "详情")
    public SysRoleEntity getInfo(Long id) {
            SysRoleEntity sysRole = sysRoleService.getById(id);
        return sysRole;
    }

}

