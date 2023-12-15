package com.lhh.serveradmin.controller.scan;

import com.lhh.serveradmin.service.scan.HoleYamlService;
import com.lhh.serverbase.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("hole/yaml")
public class HoleYamlController {

    @Autowired
    HoleYamlService holeYamlService;

    @GetMapping("page")
    public R page(@RequestParam Map<String, Object> params){
        return R.ok(holeYamlService.page(params));
    }

}
