package com.lhh.serverinfocommon.controller.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.dto.HoleNumDto;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;
import com.lhh.serverbase.vo.ScanHoleVo;
import com.lhh.serverbase.vo.ScanPortVo;
import com.lhh.serverinfocommon.service.scan.ScanSecurityHoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-09-12 15:41:27
 */
@RestController
@RequestMapping("scan/security/hole")
public class ScanSecurityHoleController {
    @Autowired
    private ScanSecurityHoleService scanSecurityHoleService;

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody ScanSecurityHoleEntity scanSecurityHole) {
        scanSecurityHoleService.save(scanSecurityHole);
    }

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<ScanSecurityHoleEntity> scanSecurityHoleList) {
        scanSecurityHoleService.saveBatch(scanSecurityHoleList);
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("update")
    public void update(@RequestBody ScanSecurityHoleEntity scanSecurityHole) {
        scanSecurityHoleService.updateById(scanSecurityHole);
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
            scanSecurityHoleService.removeById(id);
        }
    }

    /**
     * 批量删除
     *
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody List<Long> ids) {
        scanSecurityHoleService.removeByIds(ids);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<ScanSecurityHoleEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<ScanSecurityHoleEntity> scanSecurityHoleList = (List<ScanSecurityHoleEntity>) scanSecurityHoleService.listByMap(params);
        return scanSecurityHoleList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<ScanSecurityHoleEntity> page(@RequestParam Map<String, Object> params) {
        IPage<ScanSecurityHoleEntity> page = scanSecurityHoleService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("exportList")
    public List<ScanHoleVo> exportList(@RequestParam Map<String, Object> params) {
        List<ScanHoleVo> scanPortList = scanSecurityHoleService.exportList(params);
        return scanPortList;
    }

    @GetMapping("exportNum")
    public Integer exportNum(@RequestParam Map<String, Object> params) {
        Integer num = scanSecurityHoleService.exportNum(params);
        return num;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<ScanSecurityHoleEntity> list(@RequestParam Map<String, Object> params) {
        List<ScanSecurityHoleEntity> scanSecurityHoleList = scanSecurityHoleService.list(params);
        return scanSecurityHoleList;
    }

    @GetMapping("info")
    public ScanSecurityHoleEntity getInfo(Long id) {
        ScanSecurityHoleEntity scanSecurityHole = scanSecurityHoleService.getById(id);
        return scanSecurityHole;
    }

    @GetMapping("getHomeNum")
    public HomeNumDto queryHomeNum(@RequestParam Map<String, Object> params) {
        HomeNumDto dto = scanSecurityHoleService.queryHomeNum(params);
        return dto;
    }

    @PostMapping("queryHoleNum")
    public List<HoleNumDto> queryHoleNum(@RequestBody Map<String, Object> params) {
        List<HoleNumDto> list = scanSecurityHoleService.queryHoleNum(params);
        return list;
    }

}

