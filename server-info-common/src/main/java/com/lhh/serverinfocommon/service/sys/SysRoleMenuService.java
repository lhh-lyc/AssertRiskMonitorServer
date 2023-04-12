package com.lhh.serverinfocommon.service.sys;

import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.SysRoleMenuEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_角色菜单表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2022-12-28 14:21:23
 */
public interface SysRoleMenuService extends IService<SysRoleMenuEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<SysRoleMenuEntity> page(Map<String, Object> params);

    /**
     * 根据参数查询列表
     * @param params
     * @return
     */
    List<SysRoleMenuEntity> list(Map<String, Object> params);

    void delByRoleId(Long roleId);

}

