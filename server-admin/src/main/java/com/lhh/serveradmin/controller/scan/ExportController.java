package com.lhh.serveradmin.controller.scan;

import cn.allbs.excel.annotation.ExportExcel;
import cn.allbs.excel.annotation.Sheet;
import com.lhh.serveradmin.service.scan.ExportService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.vo.ScanPortVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("scan/export")
public class ExportController {

    @Autowired
    ExportService exportService;

    @PostMapping("upload")
    public R upload(@RequestPart("file") MultipartFile file){
        if (file == null) {
            return R.failed("文件不能为空");
        }
        exportService.upload2(file);
        return R.ok();
    }

    @ExportExcel(name = "用户资产", sheets = @Sheet(sheetName = "用户资产"))
    @GetMapping("exportPorts")
    public List<ScanPortVo> exportPorts(@RequestParam Map<String, Object> params){
        List<ScanPortVo> list = exportService.exportPorts(params);
        return list;
    }

}
