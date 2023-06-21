package com.lhh.serveradmin.controller.scan;

import com.lhh.serveradmin.service.scan.ReScanService;
import com.lhh.serverbase.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("scan/reScan")
public class ReScanController {

    @Autowired
    ReScanService reScanService;

    @PostMapping("reScan")
    public R rescan(@RequestBody Map<String, Object> params){
        return reScanService.rescan(params);
    }

}
