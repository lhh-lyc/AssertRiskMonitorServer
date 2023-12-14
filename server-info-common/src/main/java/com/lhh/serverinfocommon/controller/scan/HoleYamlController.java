package com.lhh.serverinfocommon.controller.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.HoleYamlEntity;
import com.lhh.serverinfocommon.service.scan.HoleYamlService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 系统_文件表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@RestController
@RequestMapping("hole/yaml")
public class HoleYamlController {
    @Autowired
    private HoleYamlService holeYamlService;

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public void save(@RequestBody HoleYamlEntity sysFiles) {
        holeYamlService.save(sysFiles);
    }

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public void save(@RequestBody List<HoleYamlEntity> sysFilesList) {
        holeYamlService.saveBatch(sysFilesList);
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public void update(@RequestBody HoleYamlEntity sysFiles) {
        holeYamlService.updateById(sysFiles);
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
            holeYamlService.removeById(id);
        }
    }

    /**
     * 批量删除
     *
     * @return
     */
    @PostMapping("deleteBatch")
    @ApiOperation(value = "批量删除")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
        holeYamlService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    @ApiOperation(value = "根据表格字段查询列表")
    public List<HoleYamlEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<HoleYamlEntity> sysFilesList = (List<HoleYamlEntity>) holeYamlService.listByMap(params);
        return sysFilesList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public IPage<HoleYamlEntity> page(@RequestParam Map<String, Object> params) {
        IPage<HoleYamlEntity> page = holeYamlService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public List<HoleYamlEntity> list(@RequestParam Map<String, Object> params) {
        List<HoleYamlEntity> sysFilesList = holeYamlService.list(params);
        return sysFilesList;
    }

    @GetMapping("info")
    @ApiOperation(value = "详情")
    public HoleYamlEntity getInfo(Long id) {
        HoleYamlEntity sysFiles = holeYamlService.getById(id);
        return sysFiles;
    }

}

