package com.lhh.servermonitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.servermonitor.service.ScanProjectService;
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
@RequestMapping("scan/project")
public class ScanProjectController {
    @Autowired
    private ScanProjectService scanProjectService;

    @PostMapping("saveProject")
    public void saveProject(@RequestBody ScanProjectEntity project) {
        scanProjectService.save(project);
        scanProjectService.saveProject(project);
    }

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody ScanProjectEntity scanProject) {
            scanProjectService.save(scanProject);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<ScanProjectEntity> scanProjectList) {
            scanProjectService.saveBatch(scanProjectList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    public void update(@RequestBody ScanProjectEntity scanProject) {
            scanProjectService.updateById(scanProject);
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @PostMapping("delete")
    public void delete(Long id) {
        if (id != null) {
                scanProjectService.removeById(id);
        }
    }

    /**
     * 批量删除
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
            scanProjectService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<ScanProjectEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<ScanProjectEntity> scanProjectList = (List<ScanProjectEntity>) scanProjectService.listByMap(params);
        return scanProjectList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<ScanProjectEntity> page(@RequestParam Map<String, Object> params) {
        IPage<ScanProjectEntity> page = scanProjectService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<ScanProjectEntity> list(@RequestParam Map<String, Object> params) {
        List<ScanProjectEntity> scanProjectList = scanProjectService.list(params);
        return scanProjectList;
    }

    @GetMapping("info")
    public ScanProjectEntity getInfo(Long id) {
            ScanProjectEntity scanProject = scanProjectService.getById(id);
        return scanProject;
    }

}

