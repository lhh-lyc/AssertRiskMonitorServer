package com.lhh.serveradmin.controller.sys;

import com.lhh.serveradmin.service.sys.SysRoleService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.SysRoleEntity;
import com.lhh.serverbase.entity.SysUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("sys/role")
public class SysRoleController {

    @Autowired
    SysRoleService sysRoleService;

    @GetMapping("page")
    public R page(@RequestParam Map<String, Object> params){
        return R.ok(sysRoleService.page(params));
    }

    @GetMapping("list")
    public R list(@RequestParam Map<String, Object> params){
        return R.ok(sysRoleService.list(params));
    }

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("/save")
    public R save(@RequestBody SysRoleEntity role) {
        try {
            sysRoleService.save(role);
        } catch (Exception e) {
            R.failed("新增失败！");
        }
        return R.ok();
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("/update")
    public R update(@RequestBody SysRoleEntity role) {
        sysRoleService.update(role);
        return R.ok();
    }

    /**
     * 删除
     *
     * @return
     */
    @PostMapping("/deleteBatch")
    public R deleteBatch(@RequestBody Long[] ids) {
        sysRoleService.deleteBatch(ids);
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{roleId}")
    public R info(@PathVariable("roleId") Long roleId) {
        SysRoleEntity role = sysRoleService.info(roleId);
        return R.ok(role);
    }

}
