package com.lhh.serveradmin.controller.scan;

import com.lhh.serveradmin.service.scan.HoleYamlFolderService;
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
@RequestMapping("hole/yaml/folder")
public class HoleYamlFolderController {

    @Autowired
    HoleYamlFolderService holeYamlFolderService;

    @GetMapping("list")
    public R list(@RequestParam Map<String, Object> params){
        return R.ok(holeYamlFolderService.list(params));
    }

}
