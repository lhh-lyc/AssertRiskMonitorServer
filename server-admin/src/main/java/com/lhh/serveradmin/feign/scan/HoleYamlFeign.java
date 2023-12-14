package com.lhh.serveradmin.feign.scan;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.HoleYamlEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface HoleYamlFeign {

    @GetMapping("/hole/yaml/page")
    IPage<HoleYamlEntity> page(@RequestParam Map<String, Object> params);

    @GetMapping("/hole/yaml/list")
    List<HoleYamlEntity> list(@RequestParam Map<String, Object> params);

    @PostMapping("/hole/yaml/saveBatch")
    void saveBatch(@RequestBody List<HoleYamlEntity> holeYamlEntityList);

}
