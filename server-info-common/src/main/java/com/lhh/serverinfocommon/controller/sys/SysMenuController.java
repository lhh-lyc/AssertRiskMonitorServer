package com.lhh.serverinfocommon.controller.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.MenuDto;
import com.lhh.serverbase.entity.SysMenuEntity;
import com.lhh.serverinfocommon.service.sys.SysMenuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统_菜单表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@RestController
@RequestMapping("sys/menu")
public class SysMenuController {
    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public void save(@RequestBody SysMenuEntity sysMenu) {
        sysMenuService.save(sysMenu);
    }

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public void save(@RequestBody List<SysMenuEntity> sysMenuList) {
        sysMenuService.saveBatch(sysMenuList);
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public void update(@RequestBody SysMenuEntity sysMenu) {
        sysMenuService.updateById(sysMenu);
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
            sysMenuService.removeById(id);
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
        if (idList != null && idList.size() > 0) {
            sysMenuService.removeByIds(idList);
        }
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    @ApiOperation(value = "根据表格字段查询列表")
    public List<SysMenuEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<SysMenuEntity> sysMenuList = (List<SysMenuEntity>) sysMenuService.listByMap(params);
        return sysMenuList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public IPage<SysMenuEntity> page(@RequestParam Map<String, Object> params) {
        IPage<SysMenuEntity> page = sysMenuService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public List<SysMenuEntity> list(@RequestParam Map<String, Object> params) {
        List<SysMenuEntity> sysMenuList = sysMenuService.list(params);
        return sysMenuList;
    }

    @GetMapping("info")
    @ApiOperation(value = "详情")
    public SysMenuEntity getInfo(Long id) {
        SysMenuEntity sysMenu = sysMenuService.getById(id);
        SysMenuEntity parentMenu = sysMenuService.getById(sysMenu.getParentId());
        sysMenu.setParentName(Const.LONG_0.equals(parentMenu.getMenuId()) ? "一级菜单" : parentMenu.getName());
        return sysMenu;
    }

    /**
     * 根据用户ID获取菜单列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/nav")
    public MenuDto getMenuNav(Long userId) {
        List<SysMenuEntity> menuList = sysMenuService.queryUserMenuList(userId);
        List<String> permissions = sysMenuService.queryAllPerms(userId);
        MenuDto menuDto = new MenuDto();
        menuDto.setMenuList(menuList);
        menuDto.setPermissions(permissions);
        return menuDto;
    }

    @GetMapping("/findAll")
    public List<SysMenuEntity> findAll() {
        return sysMenuService.findAll(null);
    }

    @GetMapping("/getChild")
    public Set<Long> getChild(Long parentId) {
        return sysMenuService.getChild(parentId);
    }

}

