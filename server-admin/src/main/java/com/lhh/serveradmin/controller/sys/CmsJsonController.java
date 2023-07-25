package com.lhh.serveradmin.controller.sys;

import com.lhh.serveradmin.service.sys.CmsJsonService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.CmsJsonEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("cms/json")
public class CmsJsonController {
    @Autowired
    private CmsJsonService cmsJsonService;

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public R page(@RequestParam Map<String, Object> params) {
        return cmsJsonService.page(params);
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public R list(@RequestParam Map<String, Object> params) {
        return cmsJsonService.list(params);
    }

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public R save(@RequestBody CmsJsonEntity cmsJson) {
        return cmsJsonService.save(cmsJson);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public R save(@RequestBody List<CmsJsonEntity> cmsJsonList) {
        return cmsJsonService.saveBatch(cmsJsonList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public R update(@RequestBody CmsJsonEntity cmsJson) {
        return cmsJsonService.update(cmsJson);
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @PostMapping("delete")
    @ApiOperation(value = "单个删除")
    public R delete(Long id) {
        return cmsJsonService.delete(id);
    }

    /**
     * 批量删除
     * @return
     */
    @PostMapping("deleteBatch")
    @ApiOperation(value = "批量删除")
    public R deleteBatch(@RequestBody Long[] ids) {
        return cmsJsonService.deleteBatch(ids);
    }

    /**
    * 详情
    * @param id
    * @return
    */
    @GetMapping("info")
    @ApiOperation(value = "详情")
    public R getInfo(Long id) {
        return cmsJsonService.info(id);
    }

}