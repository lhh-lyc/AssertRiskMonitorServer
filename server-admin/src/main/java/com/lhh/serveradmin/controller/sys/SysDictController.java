package com.lhh.serveradmin.controller.sys;

import com.lhh.serveradmin.service.sys.SysDictService;
import com.lhh.serveradmin.service.sys.SysUserService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.SysDictEntity;
import com.lhh.serverbase.entity.SysRoleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("sys/dict")
public class SysDictController {

    @Autowired
    SysDictService sysDictService;

    @GetMapping("page")
    public R page(@RequestParam Map<String, Object> params){
        return R.ok(sysDictService.page(params));
    }

    @GetMapping("list")
    public R list(@RequestParam Map<String, Object> params){
        return R.ok(sysDictService.list(params));
    }

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("/save")
    public R save(@RequestBody SysDictEntity dict) {
        try {
            sysDictService.save(dict);
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
    public R update(@RequestBody SysDictEntity dict) {
        sysDictService.update(dict);
        return R.ok();
    }

    /**
     * 删除
     *
     * @return
     */
    @PostMapping("/deleteBatch")
    public R deleteBatch(@RequestBody Long[] ids) {
        sysDictService.deleteBatch(ids);
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{userId}")
    public R info(@PathVariable("userId") Long dictId) {
        SysDictEntity dict = sysDictService.info(dictId);
        return R.ok(dict);
    }

}
