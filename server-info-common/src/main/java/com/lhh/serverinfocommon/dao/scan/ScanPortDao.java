package com.lhh.serverinfocommon.dao.scan;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.dto.GroupTagDto;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.vo.ScanPortVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-02-23 19:21:07
 */
@Mapper
public interface ScanPortDao extends BaseMapper<ScanPortEntity> {

    /**
    * 分页查询用户列表
    * @param page
    * @param params
    * @return
    */
    IPage<ScanPortEntity> queryPage(Page page, @Param("params") Map<String, Object> params);

    /**
     * 查询用户列表
     * @param params
     * @return
     */
    List<ScanPortEntity> queryList(Map<String, Object> params);

    HomeNumDto queryHomeNum(Map<String, Object> params);

    IPage<GroupTagDto> queryGroupTag(Page page, @Param("params") Map<String, Object> params);

    Integer queryGroupTagNum(@Param("params") Map<String, Object> params);

    IPage<ScanPortEntity> page(Page page, @Param("params") Map<String, Object> params);

    List<ScanPortVo> exportList(@Param("params") Map<String, Object> params);

    void deleteByIpPort(Map<String, Object> params);

    void deleteByIpList(@Param("ipLongList") List<Long> ipLongList);

    void deleteByTag(@Param("tagList") List<String> tagList, @Param("tagValueList") List<String> tagValueList);

}
