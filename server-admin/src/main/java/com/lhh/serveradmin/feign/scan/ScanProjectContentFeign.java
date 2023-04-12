package com.lhh.serveradmin.feign.scan;

import com.lhh.serverbase.entity.ScanProjectContentEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface ScanProjectContentFeign {

    @GetMapping("/scan/project/content/list")
    List<ScanProjectContentEntity> list(@RequestParam Map<String, Object> params);

    @PostMapping("/scan/project/content/deleteBatch")
    void deleteBatch(@RequestBody List<Long> idList);

}
