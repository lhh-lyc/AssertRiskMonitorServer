package com.lhh.serverinfocommon.controller.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serverbase.entity.SysRoleMenuEntity;
import com.lhh.serverinfocommon.service.sys.SysRoleMenuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 系统_角色菜单表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@RestController
@RequestMapping("sys/role/menu")
public class SysRoleMenuController {
    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public void save(@RequestBody SysRoleMenuEntity sysRoleMenu) {
        sysRoleMenuService.save(sysRoleMenu);
    }

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public void save(@RequestBody List<SysRoleMenuEntity> sysRoleMenuList) {
        if (!CollectionUtils.isEmpty(sysRoleMenuList)) {
            sysRoleMenuService.saveBatch(sysRoleMenuList);
        }
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public void update(@RequestBody SysRoleMenuEntity sysRoleMenu) {
        sysRoleMenuService.updateById(sysRoleMenu);
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
            sysRoleMenuService.removeById(id);
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
        sysRoleMenuService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    @ApiOperation(value = "根据表格字段查询列表")
    public List<SysRoleMenuEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<SysRoleMenuEntity> sysRoleMenuList = (List<SysRoleMenuEntity>) sysRoleMenuService.listByMap(params);
        return sysRoleMenuList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public IPage<SysRoleMenuEntity> page(@RequestParam Map<String, Object> params) {
        IPage<SysRoleMenuEntity> page = sysRoleMenuService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public List<SysRoleMenuEntity> list(@RequestParam Map<String, Object> params) {
        List<SysRoleMenuEntity> sysRoleMenuList = sysRoleMenuService.list(params);
        return sysRoleMenuList;
    }

    @GetMapping("info")
    @ApiOperation(value = "详情")
    public SysRoleMenuEntity getInfo(Long id) {
        SysRoleMenuEntity sysRoleMenu = sysRoleMenuService.getById(id);
        return sysRoleMenu;
    }

    @GetMapping("delByRoleId")
    @ApiOperation(value = "删除")
    public void delByRoleId(Long roleId) {
        sysRoleMenuService.delByRoleId(roleId);
    }

}

