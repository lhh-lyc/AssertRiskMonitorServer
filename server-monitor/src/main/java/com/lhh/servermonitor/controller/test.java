package com.lhh.servermonitor.controller;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.response.R;
import com.lhh.servermonitor.utils.HttpxCustomizeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
@Slf4j
public class test {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Value("${dir-setting.tool-dir}")
    private String toolDir;

    @GetMapping("test")
    public R test(String url){
        Map<String, String> result = new HashMap<>();
        try {
            result = HttpxCustomizeUtils.getUrlMap(stringRedisTemplate, toolDir, url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info(JSON.toJSONString(result));
        return R.ok().put("data", result);
    }

}
