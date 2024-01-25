package com.lhh.servermonitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.entity.ScanSecurityHoleTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户角色表
 *
 * @author lyc
 * @date 2023-12-28 17:49:19
 */
@Mapper
public interface ScanSecurityHoleTaskDao extends BaseMapper<ScanSecurityHoleTaskEntity> {

    /**
    * 分页查询用户列表
    * @param page
    * @param params
    * @return
    */
    IPage<ScanSecurityHoleTaskEntity> queryPage(Page page, @Param("params") Map<String, Object> params);

    /**
     * 查询用户列表
     * @param params
     * @return
     */
    List<ScanSecurityHoleTaskEntity> queryList(Map<String, Object> params);

}
