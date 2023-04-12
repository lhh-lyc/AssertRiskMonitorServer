package com.lhh.serveradmin.feign.sys;

import com.lhh.serverbase.dto.MenuDto;
import com.lhh.serverbase.entity.SysMenuEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(value = "infocommon")
public interface SysMenuFeign {

    @GetMapping("/sys/menu/list")
    List<SysMenuEntity> list(@RequestParam Map<String, Object> params);

    @GetMapping("/sys/menu/nav")
    MenuDto nav(@RequestParam Long userId);

    @PostMapping("/sys/menu/save")
    void save(@RequestBody SysMenuEntity sysMenu);

    @PostMapping("/sys/menu/update")
    void update(@RequestBody SysMenuEntity sysMenu);

    @PostMapping("/sys/menu/deleteBatch")
    void deleteBatch(@RequestBody Long[] ids);

    @GetMapping("/sys/menu/info")
    SysMenuEntity info(@RequestParam Long id);

    @GetMapping("/sys/menu/findAll")
    List<SysMenuEntity> findAll();

    @GetMapping("/sys/menu/getChild")
    Set<Long> getChild(@RequestParam("parentId")Long parentId);

}
