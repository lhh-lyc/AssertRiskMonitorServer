package com.lhh.servermonitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.servermonitor.service.ScanPortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("scanport")
public class ScanPortController {
    @Autowired
    private ScanPortService scanPortService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody ScanPortEntity scanPort) {
            scanPortService.save(scanPort);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<ScanPortEntity> scanPortList) {
            scanPortService.saveBatch(scanPortList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    public void update(@RequestBody ScanPortEntity scanPort) {
            scanPortService.updateById(scanPort);
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @PostMapping("delete")
    public void delete(Long id) {
        if (id != null) {
                scanPortService.removeById(id);
        }
    }

    /**
     * 批量删除
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
            scanPortService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<ScanPortEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<ScanPortEntity> scanPortList = (List<ScanPortEntity>) scanPortService.listByMap(params);
        return scanPortList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<ScanPortEntity> page(@RequestParam Map<String, Object> params) {
        IPage<ScanPortEntity> page = scanPortService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<ScanPortEntity> list(@RequestParam Map<String, Object> params) {
        List<ScanPortEntity> scanPortList = scanPortService.list(params);
        return scanPortList;
    }

    @GetMapping("info")
    public ScanPortEntity getInfo(Long id) {
            ScanPortEntity scanPort = scanPortService.getById(id);
        return scanPort;
    }

}

