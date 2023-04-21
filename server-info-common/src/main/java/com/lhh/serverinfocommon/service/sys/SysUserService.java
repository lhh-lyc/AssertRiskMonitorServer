package com.lhh.serverinfocommon.service.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.SysUserEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2022-12-28 14:21:23
 */
public interface SysUserService extends IService<SysUserEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<SysUserEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<SysUserEntity> list(Map<String, Object> params);

    SysUserEntity queryByName(String userName);

    /**
     * 查询用户的所有菜单ID
     */
    List<Long> queryAllMenuId(Long userId);

    /**
     * 获取管理员id列表
     * @return
     */
    List<Long> getAdminIdList();

}

