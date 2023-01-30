package com.lhh.serveradmin.controller;

import com.lhh.serveradmin.service.SysUserService;
import com.lhh.serverbase.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("sys/user")
public class SysUserController {

    @Autowired
    SysUserService sysUserService;

    @GetMapping("list")
    public R list(Map<String, Object> params){
        return R.ok(sysUserService.list(params));
    }

    @GetMapping("redisTest")
    public R redisTest(){
        return R.ok(sysUserService.redisTest());
    }

}
