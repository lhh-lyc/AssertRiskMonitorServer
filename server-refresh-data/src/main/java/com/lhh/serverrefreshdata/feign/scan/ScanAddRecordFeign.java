package com.lhh.serverrefreshdata.feign.scan;

import com.lhh.serverbase.entity.ScanAddRecordEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface ScanAddRecordFeign {

    @GetMapping("/scan/add/record/list")
    List<ScanAddRecordEntity> list(@RequestParam Map<String, Object> params);

}
