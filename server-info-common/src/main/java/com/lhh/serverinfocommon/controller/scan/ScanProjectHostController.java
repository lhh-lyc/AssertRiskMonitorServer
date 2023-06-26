package com.lhh.serverinfocommon.controller.scan;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.lhh.serverinfocommon.service.scan.ScanProjectHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 项目_host角色表
 *
 * @author lyc
 * @date 2023-03-06 19:24:41
 */
@RestController
@RequestMapping("scan/project/host")
public class ScanProjectHostController {
    @Autowired
    private ScanProjectHostService scanProjectHostService;

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody ScanProjectHostEntity scanProjectHost) {
        scanProjectHostService.save(scanProjectHost);
    }

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<ScanProjectHostEntity> scanProjectHostList) {
        scanProjectHostService.saveBatch(scanProjectHostList);
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("update")
    public void update(@RequestBody ScanProjectHostEntity scanProjectHost) {
        scanProjectHostService.updateById(scanProjectHost);
    }

    /**
     * 单个删除
     *
     * @param id
     * @return
     */
    @PostMapping("delete")
    public void delete(Long id) {
        if (id != null) {
            scanProjectHostService.removeById(id);
        }
    }

    /**
     * 批量删除
     *
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        scanProjectHostService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<ScanProjectHostEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<ScanProjectHostEntity> scanProjectHostList = (List<ScanProjectHostEntity>) scanProjectHostService.listByMap(params);
        return scanProjectHostList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<ScanProjectHostEntity> page(@RequestParam Map<String, Object> params) {
        IPage<ScanProjectHostEntity> page = scanProjectHostService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<ScanProjectHostEntity> list(@RequestParam Map<String, Object> params) {
        List<ScanProjectHostEntity> scanProjectHostList = scanProjectHostService.list(params);
        return scanProjectHostList;
    }

    @GetMapping("info")
    public ScanProjectHostEntity getInfo(Long id) {
        ScanProjectHostEntity scanProjectHost = scanProjectHostService.getById(id);
        return scanProjectHost;
    }

    @PostMapping("endScanDomain")
    public void endScanDomain(@RequestBody Map<String, Object> params) {
        String domain = MapUtil.getStr(params, "domain");
        scanProjectHostService.endScanDomain(domain);
    }

}

