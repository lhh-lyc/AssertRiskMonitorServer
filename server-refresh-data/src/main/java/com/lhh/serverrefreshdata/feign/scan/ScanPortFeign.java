package com.lhh.serverrefreshdata.feign.scan;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.dto.GroupTagDto;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.vo.ScanPortVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface ScanPortFeign {

    @PostMapping("/scan/port/getByIpList")
    List<ScanPortEntity> getByIpList(@RequestBody List<Long> ipList);

    @GetMapping("/scan/port/getHomeNum")
    HomeNumDto getHomeNum(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/port/getGroupTag")
    IPage<GroupTagDto> getGroupTag(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/port/getGroupTagNum")
    Integer getGroupTagNum(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/port/page")
    IPage<ScanPortEntity> page(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/port/exportList")
    List<ScanPortVo> exportList(@RequestParam Map<String, Object> params);

    @PostMapping("/scan/port/saveBatch")
    void saveBatch(@RequestBody List<ScanPortEntity> scanPortList);

    @PostMapping("/scan/port/deleteByIpPort")
    void deleteByIpPort(@RequestBody Map<String, Object> params);

    @PostMapping("/scan/port/deleteByTag")
    void deleteByTag(@RequestBody Map<String, Object> params);

}
