package com.lhh.serveradmin.controller.scan;

import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serveradmin.service.scan.ScanPortService;
import com.lhh.serveradmin.service.scan.ScanProjectService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.ScanProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("scan/port")
public class ScanPortController {

    @Autowired
    ScanPortService scanPortService;
    @Resource
    private PassJavaJwtTokenUtil jwtTokenUtil;

    @GetMapping("page")
    public R page(@RequestParam Map<String, Object> params){
        return R.ok(scanPortService.page(params));
    }

    @GetMapping("queryPage")
    public R queryPage(@RequestParam Map<String, Object> params){
        return R.ok(scanPortService.queryPage(params));
    }

    @PostMapping("delete")
    public R delete(@RequestBody Map<String, Object> params){
        scanPortService.delete(params);
        return R.ok();
    }

    @PostMapping("deleteByTag")
    public R deleteByTag(@RequestBody Map<String, Object> params){
        Long userId = Long.valueOf(jwtTokenUtil.getUserId());
        params.put("userId", userId);
        scanPortService.deleteByTag(params);
        return R.ok();
    }

}
