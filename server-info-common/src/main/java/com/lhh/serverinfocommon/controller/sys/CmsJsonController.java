package com.lhh.serverinfocommon.controller.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.CmsJsonEntity;
import com.lhh.serverinfocommon.service.sys.CmsJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-07-18 15:08:23
 */
@RestController
@RequestMapping("cms/json")
public class CmsJsonController {
    @Autowired
    private CmsJsonService cmsJsonService;

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody CmsJsonEntity cmsJson) {
        cmsJsonService.save(cmsJson);
    }

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<CmsJsonEntity> cmsJsonList) {
        cmsJsonService.saveBatch(cmsJsonList);
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("update")
    public void update(@RequestBody CmsJsonEntity cmsJson) {
        cmsJsonService.updateById(cmsJson);
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
            cmsJsonService.removeById(id);
        }
    }

    /**
     * 批量删除
     *
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
        cmsJsonService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<CmsJsonEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<CmsJsonEntity> cmsJsonList = (List<CmsJsonEntity>) cmsJsonService.listByMap(params);
        return cmsJsonList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<CmsJsonEntity> page(@RequestParam Map<String, Object> params) {
        IPage<CmsJsonEntity> page = cmsJsonService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<CmsJsonEntity> list(@RequestParam Map<String, Object> params) {
        List<CmsJsonEntity> cmsJsonList = cmsJsonService.list(params);
        return cmsJsonList;
    }

    @GetMapping("info")
    public CmsJsonEntity getInfo(Long id) {
        CmsJsonEntity cmsJson = cmsJsonService.getById(id);
        return cmsJson;
    }

    @PostMapping("updateAll")
    public void updateAll(@RequestBody List<CmsJsonEntity> cmsJsonList) {
        cmsJsonService.clearAll();
        cmsJsonService.saveBatch(cmsJsonList);
    }

}

