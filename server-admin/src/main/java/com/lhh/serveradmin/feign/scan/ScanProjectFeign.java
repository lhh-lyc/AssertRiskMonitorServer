package com.lhh.serveradmin.feign.scan;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.ScanProjectEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface ScanProjectFeign {

    @PostMapping("/scan/project/save")
    ScanProjectEntity save(@RequestBody ScanProjectEntity project);

    @GetMapping("/scan/project/basicPage")
    IPage<ScanProjectEntity> basicPage(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/project/getProjectPortNum")
    List<ScanProjectEntity> getProjectPortNum(@RequestBody List<Long> projectIdList);

    @GetMapping("/scan/project/getProjectUrlNum")
    List<ScanProjectEntity> getProjectUrlNum(@RequestBody List<Long> projectIdList);

    @GetMapping("/scan/project/page")
    IPage<ScanProjectEntity> page(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/project/list")
    List<ScanProjectEntity> list(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/project/info")
    ScanProjectEntity info(@RequestParam Long id);

    @PostMapping("/scan/project/deleteBatch")
    void deleteBatch(@RequestBody List<Long> idList);

}
