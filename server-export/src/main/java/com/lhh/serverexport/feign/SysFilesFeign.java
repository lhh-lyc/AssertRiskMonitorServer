package com.lhh.serverexport.feign;

import com.lhh.serverbase.entity.SysFilesEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "infocommon")
public interface SysFilesFeign {

    @PostMapping("/sys/files/save")
    void save(@RequestBody SysFilesEntity file);

}
