package com.lhh.serverinfocommon.controller.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverinfocommon.service.scan.ScanProjectContentService;
import com.lhh.serverinfocommon.service.scan.ScanProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


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
    @Autowired
    private ScanProjectContentService scanProjectContentService;

    @PostMapping("saveProject")
    public R saveProject(@RequestBody ScanProjectEntity project) {
        scanProjectService.save(project);
        return R.ok();
    }

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("save")
    public ScanProjectEntity save(@RequestBody ScanProjectEntity scanProject) {
        scanProjectService.save(scanProject);
        return scanProject;
    }

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<ScanProjectEntity> scanProjectList) {
        scanProjectService.saveBatch(scanProjectList);
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("update")
    public void update(@RequestBody ScanProjectEntity scanProject) {
        scanProjectService.updateById(scanProject);
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
            scanProjectService.removeById(id);
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
    @GetMapping("/basicPage")
    public IPage<ScanProjectEntity> basicPage(@RequestParam Map<String, Object> params) {
        IPage<ScanProjectEntity> page = scanProjectService.basicPage(params);
        return page;
    }

    @PostMapping("/getProjectPortNum")
    public List<ScanProjectEntity> getProjectPortNum(@RequestBody List<Long> projectIdList) {
        if (CollectionUtils.isEmpty(projectIdList)) {
            return new ArrayList<>();
        }
        List<ScanProjectEntity> scanProjectList = scanProjectService.getProjectPortNum(projectIdList);
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
     * 分页查询列表
     */
    @GetMapping("/l")
    public List<ScanProjectEntity> l(@RequestParam Map<String, Object> params) {
        Long t1 = System.currentTimeMillis();
        List<ScanProjectEntity> l = scanProjectService.l(params);
        Long t2 = System.currentTimeMillis();
        System.out.println(t2-t1);
        return l;
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
    public ScanProjectEntity getInfo(@RequestParam Long id) {
        ScanProjectEntity scanProject = scanProjectService.getById(id);
        List<ScanProjectContentEntity> list = scanProjectContentService.list(new HashMap<String, Object>(){{put("projectId", id);}});
        List<String> hostList = list.stream().map(ScanProjectContentEntity::getInputHost).collect(Collectors.toList());
        scanProject.setHosts(String.join(Const.STR_COMMA, hostList));
        return scanProject;
    }

}

