package com.lhh.serverinfocommon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.entity.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统_角色表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@Mapper
public interface SysRoleDao extends BaseMapper<SysRoleEntity> {

    /**
    * 分页查询用户列表
    * @param page
    * @param params
    * @return
    */
    IPage<SysRoleEntity> queryPage(Page page, @Param("params") Map<String, Object> params);

    /**
     * 查询用户列表
     * @param params
     * @return
     */
    List<SysRoleEntity> queryList(Map<String, Object> params);

}
