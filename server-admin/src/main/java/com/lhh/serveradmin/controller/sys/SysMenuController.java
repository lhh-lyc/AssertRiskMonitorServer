package com.lhh.serveradmin.controller.sys;

import com.lhh.serveradmin.service.sys.SysMenuService;
import com.lhh.serveradmin.service.sys.SysUserService;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.dto.MenuDto;
import com.lhh.serverbase.entity.SysMenuEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("sys/menu")
public class SysMenuController {

    @Autowired
    SysMenuService sysMenuService;

    @GetMapping("list")
    public R list(@RequestParam Map<String, Object> params){
        return R.ok(sysMenuService.list(params));
    }

    /**
     * 根据用户ID获取菜单列表
     *
     * @param userId
     * @return
     */
    @GetMapping("nav")
    public R getMenuNav(Long userId) {
        return R.ok(sysMenuService.nav(userId));
    }

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("/save")
    public R save(@RequestBody SysMenuEntity sysMenu) {
        if (sysMenu.getType() != null && StringUtils.isEmpty(sysMenu.getIcon())) {
            if (Const.INTEGER_0.equals(sysMenu.getType().intValue())) {
                sysMenu.setIcon("fa fa-cog");
            } else if (Const.INTEGER_1.equals(sysMenu.getType().intValue())) {
                sysMenu.setIcon("fa fa-file-text-o");
            }
        }
        if (sysMenu.getParentId() == null) {
            sysMenu.setParentId(Long.valueOf(0));
        }
        try {
            sysMenuService.save(sysMenu);
        } catch (Exception e) {
            R.failed("新增失败！");
        }
        return R.ok();
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("/update")
    public R update(@RequestBody SysMenuEntity sysMenu) {
        sysMenuService.update(sysMenu);
        return R.ok();
    }

    /**
     * 删除
     *
     * @return
     */
    @PostMapping("/deleteBatch")
    public R deleteBatch(@RequestBody Long[] ids) {
        sysMenuService.deleteBatch(ids);
        return R.ok();
    }

    /**
     * 菜单信息
     */
    @RequestMapping("/info/{menuId}")
    public R info(@PathVariable("menuId") Long menuId) {
        SysMenuEntity menu = sysMenuService.info(menuId);
        return R.ok(menu);
    }

    /**
     * 所有菜单信息
     */
    @RequestMapping("/findAll")
    public R findAll() {
        List<SysMenuEntity> menuList = sysMenuService.findAll();
        return R.ok(menuList);
    }

    /**
     * 查询当前节点所有子节点包括自身
     *
     * @param nodeId
     * @return
     */
    @GetMapping(value = "/getChild")
    public R getMenuAllChild(@RequestParam("nodeId") Long nodeId) {
        return R.ok(sysMenuService.getChild(nodeId));
    }

}
