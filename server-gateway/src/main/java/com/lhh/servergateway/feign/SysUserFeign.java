package com.lhh.servergateway.feign;

import com.lhh.serverbase.entity.SysUserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "infocommon")
public interface SysUserFeign {

    @GetMapping("/sys/user/queryByName")
    SysUserEntity queryByName(@RequestParam("userName") String userName);

}
