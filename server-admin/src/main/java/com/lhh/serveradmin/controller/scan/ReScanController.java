package com.lhh.serveradmin.controller.scan;

import cn.allbs.excel.annotation.ExportExcel;
import cn.allbs.excel.annotation.Sheet;
import com.lhh.serveradmin.service.scan.ExportService;
import com.lhh.serveradmin.service.scan.ReScanService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.vo.ScanPortVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
