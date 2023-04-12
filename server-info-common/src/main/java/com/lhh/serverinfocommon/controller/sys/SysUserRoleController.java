package com.lhh.serverinfocommon.controller.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.SysUserRoleEntity;
import com.lhh.serverinfocommon.service.sys.SysUserRoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 系统_用户角色表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@RestController
@RequestMapping("sys/user/role")
public class SysUserRoleController {
    @Autowired
    private SysUserRoleService sysUserRoleService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public void save(@RequestBody SysUserRoleEntity sysUserRole) {
            sysUserRoleService.save(sysUserRole);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public void save(@RequestBody List<SysUserRoleEntity> sysUserRoleList) {
            sysUserRoleService.saveBatch(sysUserRoleList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public void update(@RequestBody SysUserRoleEntity sysUserRole) {
            sysUserRoleService.updateById(sysUserRole);
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
                sysUserRoleService.removeById(id);
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
            sysUserRoleService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    @ApiOperation(value = "根据表格字段查询列表")
    public List<SysUserRoleEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<SysUserRoleEntity> sysUserRoleList = (List<SysUserRoleEntity>) sysUserRoleService.listByMap(params);
        return sysUserRoleList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public IPage<SysUserRoleEntity> page(@RequestParam Map<String, Object> params) {
        IPage<SysUserRoleEntity> page = sysUserRoleService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public List<SysUserRoleEntity> list(@RequestParam Map<String, Object> params) {
        List<SysUserRoleEntity> sysUserRoleList = sysUserRoleService.list(params);
        return sysUserRoleList;
    }

    @GetMapping("info")
    @ApiOperation(value = "详情")
    public SysUserRoleEntity getInfo(Long id) {
            SysUserRoleEntity sysUserRole = sysUserRoleService.getById(id);
        return sysUserRole;
    }

}

