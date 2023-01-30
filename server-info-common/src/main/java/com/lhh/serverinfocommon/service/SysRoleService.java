package com.lhh.serverinfocommon.service;

import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.SysRoleEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_角色表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2022-12-28 14:21:23
 */
public interface SysRoleService extends IService<SysRoleEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<SysRoleEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<SysRoleEntity> list(Map<String, Object> params);

}

