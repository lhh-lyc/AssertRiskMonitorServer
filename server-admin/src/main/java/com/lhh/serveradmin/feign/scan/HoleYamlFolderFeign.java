package com.lhh.serveradmin.feign.scan;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.HoleYamlFolderEntity;
import com.lhh.serverbase.entity.HoleYamlFolderEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface HoleYamlFolderFeign {

    @GetMapping("/hole/yaml/folder/page")
    IPage<HoleYamlFolderEntity> page(@RequestParam Map<String, Object> params);

    @PostMapping("/hole/yaml/folder/list")
    List<HoleYamlFolderEntity> list(@RequestBody Map<String, Object> params);

    @PostMapping("/hole/yaml/folder/saveBatch")
    void saveBatch(@RequestBody List<HoleYamlFolderEntity> HoleYamlFolderEntityList);

    @PostMapping("/hole/yaml/folder/save")
    HoleYamlFolderEntity save(@RequestBody HoleYamlFolderEntity holeYamlFolderEntity);

    @PostMapping("/hole/yaml/folder/deleteBatch")
    void deleteBatch(@RequestBody List<Long> ids);

}
