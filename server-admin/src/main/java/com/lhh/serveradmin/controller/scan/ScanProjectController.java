package com.lhh.serveradmin.controller.scan;

import com.lhh.serveradmin.service.scan.ScanProjectService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.entity.ScanProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("scan/project")
public class ScanProjectController {

    @Autowired
    ScanProjectService projectService;

    @PostMapping("saveProject")
    public R saveProject(@RequestBody ScanProjectEntity project){
        return projectService.saveProject(project);
    }

    @GetMapping("page")
    public R page(@RequestParam Map<String, Object> params){
        return R.ok(projectService.page(params));
    }

    @GetMapping("info/{id}")
    public R info(@PathVariable("id") Long id){
        ScanProjectEntity project = projectService.info(id);
        return R.ok(project);
    }

    @PostMapping("delete")
    public R delete(@RequestBody List<Long> ids){
        projectService.delete(ids);
        return R.ok();
    }

}
