package com.lhh.serveradmin.feign.scan;

import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.dto.ScanResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface ScanPortFeign {

    @GetMapping("/scan/port/getHomeNum")
    HomeNumDto getHomeNum(@RequestParam Map<String, Object> params);

}
