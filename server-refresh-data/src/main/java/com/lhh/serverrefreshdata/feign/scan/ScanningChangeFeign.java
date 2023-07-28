package com.lhh.serverrefreshdata.feign.scan;

import com.lhh.serverbase.entity.NetErrorDataEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface ScanningChangeFeign {

    @GetMapping("/net/error/data/list")
    List<NetErrorDataEntity> list(@RequestParam Map<String, Object> params);

    @PostMapping("/net/error/data/deleteBatch")
    void delErrorData(@RequestBody List<Long> ids);

    @PostMapping("/scan/project/host/endScanDomain")
    void endScanDomain(@RequestBody Map<String, Object> params);

    @PostMapping("/scan/host/endScanIp")
    void endScanIp(@RequestBody Map<String, Object> params);

}
