package com.lhh.serverReScan.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统_用户角色表
 *
 * @author lyc
 * @date 2023-03-06 19:24:41
 */
@Mapper
public interface ScanProjectHostDao extends BaseMapper<ScanProjectHostEntity> {

    ScanProjectHostEntity queryByHost(@Param("domain") String domain);

    List<ScanProjectHostEntity> queryByHostList(@Param("hostList") List<String> hostList);

    List<ScanProjectHostEntity> queryProjectByParent(@Param("parentDomain") String parentDomain);

}
