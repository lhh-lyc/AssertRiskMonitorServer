package com.lhh.serveradmin.feign;

import com.lhh.serverbase.entity.SysUserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface SysUserFeign {

    @GetMapping("/sys/user/list")
    List<SysUserEntity> list(@RequestParam Map<String, Object> params);

}
