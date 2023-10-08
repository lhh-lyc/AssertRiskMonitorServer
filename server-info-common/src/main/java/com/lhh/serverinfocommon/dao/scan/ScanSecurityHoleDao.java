package com.lhh.serverinfocommon.dao.scan;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;
import com.lhh.serverbase.vo.ScanHoleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-09-12 15:41:27
 */
@Mapper
public interface ScanSecurityHoleDao extends BaseMapper<ScanSecurityHoleEntity> {

    /**
     * 分页查询用户列表
     *
     * @param page
     * @param params
     * @return
     */
    IPage<ScanSecurityHoleEntity> queryPage(Page page, @Param("params") Map<String, Object> params);

    IPage<ScanHoleVo> exportList(Page page, @Param("params") Map<String, Object> params);

    Integer exportNum(@Param("params") Map<String, Object> params);

    /**
     * 查询用户列表
     *
     * @param params
     * @return
     */
    List<ScanSecurityHoleEntity> queryList(Map<String, Object> params);

    List<ScanSecurityHoleEntity> basicList(Map<String, Object> params);

    HomeNumDto queryHomeNum(Map<String, Object> params);

}
