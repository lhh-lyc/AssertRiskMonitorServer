package com.lhh.serverinfocommon.service.sys;

import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.SysUserRoleEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户角色表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2022-12-28 14:21:23
 */
public interface SysUserRoleService extends IService<SysUserRoleEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<SysUserRoleEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<SysUserRoleEntity> list(Map<String, Object> params);

}

