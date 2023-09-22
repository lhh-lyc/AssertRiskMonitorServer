package com.lhh.serveradmin.feign.scan;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.dto.KeyValueDto;
import com.lhh.serverbase.dto.ScanResultDto;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.serverbase.entity.ScanHostEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "infocommon")
public interface ScanHostFeign {

    @GetMapping("/scan/host/page")
    IPage<ScanHostEntity> page(@RequestParam Map<String, Object> params);

    @PostMapping("/scan/host/getByDomainList")
    List<ScanHostEntity> getByDomainList(@RequestBody List<String> hostList);

    @PostMapping("/scan/host/getByIpList")
    List<ScanHostEntity> getByIpList(@RequestBody List<Long> hostList);

    @GetMapping("/scan/host/getDomainGroupList")
    List<ScanResultDto> getDomainGroupList(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/host/equalParams")
    List<ScanHostEntity> equalParams(@RequestParam Map<String, Object> params);

    @PostMapping("/scan/host/saveBatch")
    void saveBatch(@RequestBody List<ScanHostEntity> scanHostList);

    @PostMapping("/scan/host/update")
    void update(@RequestBody ScanHostEntity scanHost);

    @GetMapping("/scan/host/getCompanyNum")
    Integer getCompanyNum(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/host/getDomainNum")
    Integer getDomainNum(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/host/getSubDomainNum")
    Integer getSubDomainNum(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/host/companyRanking")
    List<KeyValueDto> companyRanking(@RequestParam Map<String, Object> params);

    @PostMapping("/scan/host/getParentDomainList")
    List<HostCompanyEntity> getParentDomainList(@RequestBody Map<String, Object> params);

    @GetMapping("/scan/host/getParentList")
    List<String> getParentList();

}
