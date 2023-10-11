package com.lhh.serveradmin.feign.sys;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.SysFilesEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "infocommon")
public interface SysFilesFeign {

    @GetMapping("/sys/files/page")
    IPage<SysFilesEntity> page(@RequestParam Map<String, Object> params);

}
