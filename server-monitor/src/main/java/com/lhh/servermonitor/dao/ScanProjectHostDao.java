package com.lhh.servermonitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户角色表
 *
 * @author lyc
 * @date 2023-03-06 19:24:41
 */
@Mapper
public interface ScanProjectHostDao extends BaseMapper<ScanProjectHostEntity> {

    void updateEndScanDomain(@Param("domain") String domain);

}
