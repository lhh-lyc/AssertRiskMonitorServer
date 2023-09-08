package com.lhh.serveradmin.controller.scan;

import com.lhh.serveradmin.service.scan.ScanHomeService;
import com.lhh.serverbase.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("scan/home")
public class ScanHomeController {

    @Autowired
    ScanHomeService scanHomeService;

    @GetMapping("getHomeNum")
    public R getHomeNum(@RequestParam Map<String, Object> params){
        return R.ok().put("data", scanHomeService.getHomeNum(params));
    }

    @GetMapping("getUnHomeNum")
    public R getUnHomeNum(@RequestParam Map<String, Object> params){
        return R.ok().put("data", scanHomeService.getUnHomeNum(params));
    }

    @GetMapping("getRecordList")
    public R getRecordList(@RequestParam Map<String, Object> params){
        return R.ok(scanHomeService.getRecordList(params));
    }

    @GetMapping("getGroupTag")
    public R getGroupTag(@RequestParam Map<String, Object> params){
        return R.ok(scanHomeService.getGroupTag(params));
    }

    @GetMapping("companyRanking")
    public R companyRanking(@RequestParam Map<String, Object> params){
        return R.ok(scanHomeService.companyRanking(params));
    }

}
