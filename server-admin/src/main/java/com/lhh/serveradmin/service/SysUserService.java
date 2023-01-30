package com.lhh.serveradmin.service;

import com.lhh.serveradmin.feign.SysUserFeign;
import com.lhh.serveradmin.utils.JedisUtils;
import com.lhh.serverbase.entity.SysUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SysUserService {

    @Autowired
    SysUserFeign sysUserFeign;

    public List<SysUserEntity> list(Map<String, Object> params){
        List<SysUserEntity> list = sysUserFeign.list(params);
        return list;
    }

    public String redisTest(){
        return JedisUtils.getJson("CM_UNIT_MAP");
    }

}
