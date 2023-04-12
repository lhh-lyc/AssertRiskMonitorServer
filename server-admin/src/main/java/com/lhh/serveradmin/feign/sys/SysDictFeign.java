package com.lhh.serveradmin.feign.sys;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.SysDictEntity;
import com.lhh.serverbase.entity.SysMenuEntity;
import com.lhh.serverbase.entity.SysUserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface SysDictFeign {

    @GetMapping("/sys/dict/page")
    IPage<SysDictEntity> page(@RequestParam Map<String, Object> params);

    @GetMapping("/sys/dict/list")
    List<SysDictEntity> list(@RequestParam Map<String, Object> params);

    @GetMapping("/sys/dict/info")
    SysDictEntity info(@RequestParam("id") Long id);

    @PostMapping("/sys/dict/save")
    void save(@RequestBody SysDictEntity dict);

    @PostMapping("/sys/dict/update")
    void update(@RequestBody SysDictEntity dict);

    @PostMapping("/sys/dict/deleteBatch")
    void deleteBatch(@RequestBody Long[] ids);

}
