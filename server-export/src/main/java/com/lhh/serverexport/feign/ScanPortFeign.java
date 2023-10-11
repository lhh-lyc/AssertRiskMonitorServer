package com.lhh.serverexport.feign;

import com.lhh.serverbase.vo.ScanPortVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface ScanPortFeign {

    @GetMapping("/scan/port/exportList")
    List<ScanPortVo> exportList(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/port/exportNum")
    Integer exportNum(@RequestParam Map<String, Object> params);

}
