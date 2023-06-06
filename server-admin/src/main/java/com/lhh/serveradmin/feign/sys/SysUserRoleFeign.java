package com.lhh.serveradmin.feign.sys;

import com.lhh.serverbase.entity.SysUserRoleEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface SysUserRoleFeign {

    @GetMapping("/sys/user/role/list")
    List<SysUserRoleEntity> list(@RequestParam Map<String, Object> params);

    @PostMapping("sys/user/role/saveBatch")
    void saveBatch(@RequestBody List<SysUserRoleEntity> SysUserRoleList);

}
