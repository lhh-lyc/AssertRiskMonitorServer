package com.lhh.servermonitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.ScanAddRecordEntity;
import com.lhh.servermonitor.service.ScanAddRecordService;
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
@RequestMapping("scanaddrecord")
public class ScanAddRecordController {
    @Autowired
    private ScanAddRecordService scanAddRecordService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody ScanAddRecordEntity scanAddRecord) {
            scanAddRecordService.save(scanAddRecord);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<ScanAddRecordEntity> scanAddRecordList) {
            scanAddRecordService.saveBatch(scanAddRecordList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    public void update(@RequestBody ScanAddRecordEntity scanAddRecord) {
            scanAddRecordService.updateById(scanAddRecord);
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @PostMapping("delete")
    public void delete(Long id) {
        if (id != null) {
                scanAddRecordService.removeById(id);
        }
    }

    /**
     * 批量删除
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
            scanAddRecordService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<ScanAddRecordEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<ScanAddRecordEntity> scanAddRecordList = (List<ScanAddRecordEntity>) scanAddRecordService.listByMap(params);
        return scanAddRecordList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<ScanAddRecordEntity> page(@RequestParam Map<String, Object> params) {
        IPage<ScanAddRecordEntity> page = scanAddRecordService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<ScanAddRecordEntity> list(@RequestParam Map<String, Object> params) {
        List<ScanAddRecordEntity> scanAddRecordList = scanAddRecordService.list(params);
        return scanAddRecordList;
    }

    @GetMapping("info")
    public ScanAddRecordEntity getInfo(Long id) {
            ScanAddRecordEntity scanAddRecord = scanAddRecordService.getById(id);
        return scanAddRecord;
    }

}

