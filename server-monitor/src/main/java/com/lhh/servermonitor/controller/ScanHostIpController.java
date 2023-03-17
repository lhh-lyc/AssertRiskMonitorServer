package com.lhh.servermonitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.ScanHostIpEntity;
import com.lhh.servermonitor.service.ScanHostIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 系统_用户角色表
 *
 * @author lyc
 * @date 2023-03-16 17:05:59
 */
@RestController
@RequestMapping("scanhostip")
public class ScanHostIpController {
    @Autowired
    private ScanHostIpService scanHostIpService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody ScanHostIpEntity scanHostIp) {
            scanHostIpService.save(scanHostIp);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<ScanHostIpEntity> scanHostIpList) {
            scanHostIpService.saveBatch(scanHostIpList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    public void update(@RequestBody ScanHostIpEntity scanHostIp) {
            scanHostIpService.updateById(scanHostIp);
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @PostMapping("delete")
    public void delete(Long id) {
        if (id != null) {
                scanHostIpService.removeById(id);
        }
    }

    /**
     * 批量删除
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
            scanHostIpService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<ScanHostIpEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<ScanHostIpEntity> scanHostIpList = (List<ScanHostIpEntity>) scanHostIpService.listByMap(params);
        return scanHostIpList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<ScanHostIpEntity> page(@RequestParam Map<String, Object> params) {
        IPage<ScanHostIpEntity> page = scanHostIpService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<ScanHostIpEntity> list(@RequestParam Map<String, Object> params) {
        List<ScanHostIpEntity> scanHostIpList = scanHostIpService.list(params);
        return scanHostIpList;
    }

    @GetMapping("info")
    public ScanHostIpEntity getInfo(Long id) {
            ScanHostIpEntity scanHostIp = scanHostIpService.getById(id);
        return scanHostIp;
    }

}

