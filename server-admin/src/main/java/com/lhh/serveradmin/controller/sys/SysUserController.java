package com.lhh.serveradmin.controller.sys;

import com.lhh.serveradmin.service.sys.SysUserService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.SysMenuEntity;
import com.lhh.serverbase.entity.SysUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("sys/user")
public class SysUserController {

    @Autowired
    SysUserService sysUserService;

    @GetMapping("page")
    public R page(@RequestParam Map<String, Object> params){
        return R.ok(sysUserService.page(params));
    }

    @GetMapping("list")
    public R list(@RequestParam Map<String, Object> params){
        return R.ok(sysUserService.list(params));
    }

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("/save")
    public R save(@RequestBody SysUserEntity user) {
        try {
            sysUserService.save(user);
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
    public R update(@RequestBody SysUserEntity user) {
        sysUserService.update(user);
        return R.ok();
    }

    /**
     * 删除
     *
     * @return
     */
    @PostMapping("/deleteBatch")
    public R deleteBatch(@RequestBody Long[] ids) {
        sysUserService.deleteBatch(ids);
        return R.ok();
    }

    /**
     * 用户信息
     */
    @RequestMapping("/info/{userId}")
    public R info(@PathVariable("userId") Long userId) {
        SysUserEntity user = sysUserService.info(userId);
        return R.ok(user);
    }

}
