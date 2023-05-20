package com.lhh.servermonitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户角色表
 *
 * @author lyc
 * @date 2023-03-06 19:24:41
 */
@Mapper
public interface ScanProjectContentDao extends BaseMapper<ScanProjectContentEntity> {

    List<ScanProjectContentEntity> getUnCompletedIdList();

    List<ScanProjectContentEntity> getContentIpList(List<String> notIdList);

    void updateStatus(List<ScanProjectContentEntity> list);

    void updateEndScanContent(@Param("ipLong") Long ipLong);

}
