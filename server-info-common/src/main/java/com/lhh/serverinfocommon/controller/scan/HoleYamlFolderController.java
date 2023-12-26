package com.lhh.serverinfocommon.controller.scan;

import com.lhh.serverbase.entity.HoleYamlFolderEntity;
import com.lhh.serverinfocommon.service.scan.HoleYamlFolderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统_文件表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@RestController
@RequestMapping("hole/yaml/folder")
public class HoleYamlFolderController {
    @Autowired
    private HoleYamlFolderService holeYamlFolderService;

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public HoleYamlFolderEntity save(@RequestBody HoleYamlFolderEntity folder) {
        holeYamlFolderService.save(folder);
        return folder;
    }

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public void save(@RequestBody List<HoleYamlFolderEntity> sysFilesList) {
        holeYamlFolderService.saveBatch(sysFilesList);
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public void update(@RequestBody HoleYamlFolderEntity sysFiles) {
        holeYamlFolderService.updateById(sysFiles);
    }

    /**
     * 单个删除
     *
     * @param id
     * @return
     */
    @PostMapping("delete")
    @ApiOperation(value = "单个删除")
    public void delete(Long id) {
        if (id != null) {
            holeYamlFolderService.removeById(id);
        }
    }

    /**
     * 批量删除
     *
     * @return
     */
    @PostMapping("deleteBatch")
    @ApiOperation(value = "批量删除")
    public void deleteBatch(@RequestBody List<Long> ids) {
        holeYamlFolderService.removeByIds(ids);
    }

    /**
     * 根据条件查询列表数据
     */
    @PostMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public List<HoleYamlFolderEntity> list(@RequestBody Map<String, Object> params) {
        List<HoleYamlFolderEntity> sysFilesList = holeYamlFolderService.list(params);
        return sysFilesList;
    }

    @GetMapping("info")
    @ApiOperation(value = "详情")
    public HoleYamlFolderEntity getInfo(@RequestParam Long id) {
        HoleYamlFolderEntity sysFiles = holeYamlFolderService.getById(id);
        return sysFiles;
    }

}

