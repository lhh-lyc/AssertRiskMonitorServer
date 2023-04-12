package com.lhh.serveradmin.feign.sys;

import com.lhh.serverbase.entity.SysRoleMenuEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface SysRoleMenuFeign {

    @GetMapping("/sys/role/menu/list")
    List<SysRoleMenuEntity> list(@RequestParam Map<String, Object> params);

    @PostMapping("/sys/role/menu/saveBatch")
    void saveBatch(@RequestBody List<SysRoleMenuEntity> sysRoleMenuList);

    @GetMapping("/sys/role/menu/delByRoleId")
    void delByRoleId(@RequestParam Long roleId);

}
