package com.lhh.serverrefreshdata.feign.scan;

import com.lhh.serverbase.entity.ScanProjectHostEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface ScanProjectHostFeign {

    @GetMapping("/scan/project/host/list")
    List<ScanProjectHostEntity> list(@RequestParam Map<String, Object> params);

    @PostMapping("/scan/project/host/deleteBatch")
    void deleteBatch(@RequestBody List<Long> idList);

}
