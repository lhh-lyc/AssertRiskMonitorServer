package com.lhh.serverinfocommon.dao.scan;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.entity.ScanProjectEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-02-23 19:21:08
 */
@Mapper
public interface ScanProjectDao extends BaseMapper<ScanProjectEntity> {

    /**
    * 分页查询用户列表
    * @param page
    * @param params
    * @return
    */
    IPage<ScanProjectEntity> basicPage(Page page, @Param("params") Map<String, Object> params);

    List<ScanProjectEntity> getProjectPortNum(@Param("projectIdList") List<Long> projectIdList);

    List<ScanProjectEntity> getProjectUrlNum(@Param("projectIdList") List<Long> projectIdList);

    IPage<ScanProjectEntity> queryPage(Page page, @Param("params") Map<String, Object> params);

    /**
     * 查询用户列表
     * @param params
     * @return
     */
    List<ScanProjectEntity> queryList(@Param("params") Map<String, Object> params);

}
