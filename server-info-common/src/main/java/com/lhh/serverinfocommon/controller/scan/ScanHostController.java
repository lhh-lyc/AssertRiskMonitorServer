package com.lhh.serverinfocommon.controller.scan;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.dto.KeyValueDto;
import com.lhh.serverbase.dto.ScanResultDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverinfocommon.service.scan.ScanHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-02-23 19:29:15
 */
@RestController
@RequestMapping("scan/host")
public class ScanHostController {
    @Autowired
    private ScanHostService scanHostService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody ScanHostEntity scanHost) {
            scanHostService.save(scanHost);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<ScanHostEntity> scanHostList) {
            scanHostService.saveBatch(scanHostList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    public void update(@RequestBody ScanHostEntity scanHost) {
            scanHostService.updateById(scanHost);
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @PostMapping("delete")
    public void delete(Long id) {
        if (id != null) {
                scanHostService.removeById(id);
        }
    }

    /**
     * 批量删除
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
            scanHostService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<ScanHostEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<ScanHostEntity> scanHostList = (List<ScanHostEntity>) scanHostService.listByMap(params);
        return scanHostList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<ScanHostEntity> page(@RequestParam Map<String, Object> params) {
        IPage<ScanHostEntity> page = scanHostService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<ScanHostEntity> list(@RequestParam Map<String, Object> params) {
        List<ScanHostEntity> scanHostList = scanHostService.list(params);
        return scanHostList;
    }

    @GetMapping("info")
    public ScanHostEntity getInfo(Long id) {
            ScanHostEntity scanHost = scanHostService.getById(id);
        return scanHost;
    }

    @PostMapping("getByDomainList")
    public List<ScanHostEntity> getByDomainList(@RequestBody List<String> hostList) {
        List<ScanHostEntity> scanHostList = scanHostService.getByDomainList(hostList);
        return scanHostList;
    }

    @PostMapping("getByIpList")
    public List<ScanHostEntity> getByIpList(@RequestBody List<Long> hostList) {
        List<ScanHostEntity> scanHostList = scanHostService.getByIpList(hostList);
        return scanHostList;
    }

    @GetMapping("getDomainGroupList")
    public List<ScanResultDto> queryDomainGroupList(@RequestParam Map<String, Object> params) {
        List<ScanResultDto> scanHostList = scanHostService.queryDomainGroupList(params);
        return scanHostList;
    }

    @GetMapping("equalParams")
    public List<ScanHostEntity> equalParams(@RequestParam Map<String, Object> params) {
        List<ScanHostEntity> scanHostList = scanHostService.equalParams(params);
        return scanHostList;
    }

    @GetMapping("getCompanyNum")
    public Integer getCompanyNum(@RequestParam Map<String, Object> params) {
        Integer num = scanHostService.getCompanyNum(params);
        return num;
    }

    @GetMapping("getDomainNum")
    public Integer getDomainNum(@RequestParam Map<String, Object> params) {
        Integer num = scanHostService.getDomainNum(params);
        return num;
    }

    @GetMapping("getSubDomainNum")
    public Integer getSubDomainNum(@RequestParam Map<String, Object> params) {
        Integer num = scanHostService.getSubDomainNum(params);
        return num;
    }

    @PostMapping("getParentDomainList")
    public List<String> getParentDomainList(@RequestBody Map<String, Object> params) {
        List<String> list = scanHostService.getParentDomainList(params);
        return list;
    }

    @GetMapping("companyRanking")
    public List<KeyValueDto> companyRanking(@RequestParam Map<String, Object> params) {
        List<KeyValueDto> list = scanHostService.companyRanking(params);
        return list;
    }

    @PostMapping("endScanIp")
    public void endScanIp(@RequestBody Map<String, Object> params) {
        Long ip = MapUtil.getLong(params, "ipLong");
        String scanPorts = MapUtil.getStr(params, "scanPorts");
        scanHostService.endScanIp(ip, scanPorts);
    }

}

