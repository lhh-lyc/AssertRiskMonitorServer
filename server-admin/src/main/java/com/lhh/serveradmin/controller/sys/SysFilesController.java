package com.lhh.serveradmin.controller.sys;

import com.lhh.serveradmin.service.sys.SysFilesService;
import com.lhh.serverbase.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("sys/files")
public class SysFilesController {

    @Autowired
    SysFilesService sysFilesService;

    @GetMapping("page")
    public R page(@RequestParam Map<String, Object> params){
        return R.ok(sysFilesService.page(params));
    }

}
