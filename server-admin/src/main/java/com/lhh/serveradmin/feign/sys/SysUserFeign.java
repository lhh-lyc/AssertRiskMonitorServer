package com.lhh.serveradmin.feign.sys;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.SysUserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface SysUserFeign {

    @GetMapping("/sys/user/page")
    IPage<SysUserEntity> page(@RequestParam Map<String, Object> params);

    @GetMapping("/sys/user/list")
    List<SysUserEntity> list(@RequestParam Map<String, Object> params);

    @GetMapping("/sys/user/queryByName")
    SysUserEntity queryByName(@RequestParam("userName") String userName);

    @GetMapping("/sys/user/info")
    SysUserEntity info(@RequestParam("id") Long id);

    @PostMapping("/sys/user/save")
    void save(@RequestBody SysUserEntity sysUser);

    @PostMapping("/sys/user/update")
    void update(@RequestBody SysUserEntity sysUser);

    @PostMapping("/sys/menu/deleteBatch")
    void deleteBatch(@RequestBody Long[] ids);

}
