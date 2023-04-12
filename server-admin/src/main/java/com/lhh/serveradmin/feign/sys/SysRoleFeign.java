package com.lhh.serveradmin.feign.sys;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.SysRoleEntity;
import com.lhh.serverbase.entity.SysUserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface SysRoleFeign {

    @GetMapping("/sys/role/page")
    IPage<SysRoleEntity> page(@RequestParam Map<String, Object> params);

    @GetMapping("/sys/role/list")
    List<SysRoleEntity> list(@RequestParam Map<String, Object> params);

    @GetMapping("/sys/role/save")
    void save(@RequestBody SysRoleEntity role);

    @PostMapping("/sys/role/update")
    void update(@RequestBody SysRoleEntity role);

    @PostMapping("/sys/role/deleteBatch")
    void deleteBatch(@RequestBody Long[] ids);

    @GetMapping("/sys/role/info")
    SysRoleEntity info(@RequestParam("id") Long id);

}
