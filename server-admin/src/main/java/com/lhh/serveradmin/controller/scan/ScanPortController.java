package com.lhh.serveradmin.controller.scan;

import com.lhh.serveradmin.service.scan.ScanPortService;
import com.lhh.serveradmin.service.scan.ScanProjectService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.ScanProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("scan/port")
public class ScanPortController {

    @Autowired
    ScanPortService scanPortService;

    @GetMapping("page")
    public R page(@RequestParam Map<String, Object> params){
        return R.ok(scanPortService.page(params));
    }

    @PostMapping("delete")
    public R delete(@RequestBody Map<String, Object> params){
        scanPortService.delete(params);
        return R.ok();
    }

}
