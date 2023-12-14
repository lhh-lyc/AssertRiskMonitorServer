package com.lhh.serveradmin.controller.scan;

import cn.allbs.excel.annotation.ExportExcel;
import cn.allbs.excel.annotation.Sheet;
import com.lhh.serveradmin.service.scan.ExportService;
import com.lhh.serverbase.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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

    @PostMapping("uploadCms")
    public R uploadCms(@RequestPart("file") MultipartFile file){
        if (file == null) {
            return R.failed("文件不能为空");
        }
        exportService.uploadCms(file);
        return R.ok();
    }

    @PostMapping("uploadFiles")
    public R uploadFiles(@RequestPart("files") List<MultipartFile> files){
        if (CollectionUtils.isEmpty(files)) {
            return R.failed("文件不能为空");
        }
        exportService.uploadFiles(files);
        return R.ok();
    }

    @ExportExcel(name = "用户资产", sheets = @Sheet(sheetName = "用户资产"))
    @GetMapping("exportPorts")
    public void exportPorts(@RequestParam Map<String, Object> params, HttpServletResponse response){
        exportService.exportPorts(params, response);
    }

    @ExportExcel(name = "漏洞资产", sheets = @Sheet(sheetName = "漏洞资产"))
    @GetMapping("exportHoles")
    public void exportHoles(@RequestParam Map<String, Object> params, HttpServletResponse response){
        exportService.exportHoles(params, response);
    }

    @GetMapping("uploadPorts")
    public R uploadPorts(@RequestParam Map<String, Object> params){
        exportService.uploadPorts(params);
        return R.ok();
    }

    @GetMapping("uploadHoles")
    public R uploadHoles(@RequestParam Map<String, Object> params){
        exportService.uploadHoles(params);
        return R.ok();
    }

    @PostMapping("exportFiles")
    public R exportFiles(@RequestBody List<String> urlList){
        return exportService.exportFiles(urlList);
    }

}
