package com.lhh.serveradmin.feign.sys;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.CmsJsonEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-07-18 15:08:23
 */
@FeignClient(name = "infocommon")
public interface CmsJsonFeign {

    /**
     * 查询 分页数据
     *
     * @param
     */
    @GetMapping("cmsjson/page")
    IPage<CmsJsonEntity> page(@RequestParam Map<String, Object> params);

    /**
     * 查询 列表数据
     *
     * @param
     */
    @GetMapping("cms/json/list")
    List<CmsJsonEntity> list(@RequestParam Map<String, Object> params);

    /**
     * 保存
     */
    @PostMapping("cmsjson/save")
    void save(@RequestBody CmsJsonEntity cmsJson);

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("cms/json/saveBatch")
    void saveBatch(@RequestBody List<CmsJsonEntity> CmsJsonList);

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("cms/json/update")
    void update(@RequestBody CmsJsonEntity cmsJson);

    /**
     * 单个逻辑删除
     *
     * @param id
     * @return
     */
    @PostMapping("cmsjson/delete")
    void delete(Long id);

    /**
     * 批量逻辑删除
     *
     * @return
     */
    @PostMapping("cms/json/deleteBatch")
    void deleteBatch(@RequestBody Long[] ids);

    @PostMapping("cms/json/updateAll")
    void updateAll(@RequestBody List<CmsJsonEntity> CmsJsonList);

    @GetMapping("cmsjson/info")
    CmsJsonEntity info(@RequestParam(name = "id") Long id);

}


