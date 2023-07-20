package com.lhh.serverinfocommon.controller.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.ScanHostPortEntity;
import com.lhh.serverinfocommon.service.scan.ScanHostPortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-07-12 15:38:11
 */
@RestController
@RequestMapping("scan/host/port")
public class ScanHostPortController {
    @Autowired
    private ScanHostPortService scanHostPortService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody ScanHostPortEntity scanHostPort) {
            scanHostPortService.save(scanHostPort);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<ScanHostPortEntity> scanHostPortList) {
            scanHostPortService.saveBatch(scanHostPortList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    public void update(@RequestBody ScanHostPortEntity scanHostPort) {
            scanHostPortService.updateById(scanHostPort);
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @PostMapping("delete")
    public void delete(Long id) {
        if (id != null) {
                scanHostPortService.removeById(id);
        }
    }

    /**
     * 批量删除
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
            scanHostPortService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<ScanHostPortEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<ScanHostPortEntity> scanHostPortList = (List<ScanHostPortEntity>) scanHostPortService.listByMap(params);
        return scanHostPortList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<ScanHostPortEntity> page(@RequestParam Map<String, Object> params) {
        IPage<ScanHostPortEntity> page = scanHostPortService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<ScanHostPortEntity> list(@RequestParam Map<String, Object> params) {
        List<ScanHostPortEntity> scanHostPortList = scanHostPortService.list(params);
        return scanHostPortList;
    }

    @GetMapping("info")
    public ScanHostPortEntity getInfo(Long id) {
            ScanHostPortEntity scanHostPort = scanHostPortService.getById(id);
        return scanHostPort;
    }

}

