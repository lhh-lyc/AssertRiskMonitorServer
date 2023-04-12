package com.lhh.serverinfocommon.dao.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.entity.SysMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统_菜单表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@Mapper
public interface SysMenuDao extends BaseMapper<SysMenuEntity> {

    /**
    * 分页查询用户列表
    * @param page
    * @param params
    * @return
    */
    IPage<SysMenuEntity> queryPage(Page page, @Param("params") Map<String, Object> params);

    /**
     * 查询用户列表
     * @param params
     * @return
     */
    List<SysMenuEntity> queryList(Map<String, Object> params);

    /**
     * admin用户权限
     *
     * @return
     */
    List<String> queryAllPerms();

    /**
     * 根据用户Id查询用户权限
     *
     * @param userId
     * @return
     */
    List<String> queryAllPermsByUserId(Long userId);

    /**
     * 根据父菜单，查询子菜单
     *
     * @param parentId 父菜单ID
     */
    List<SysMenuEntity> queryListParentId(Long parentId);

    List<SysMenuEntity> queryAllList();

}
