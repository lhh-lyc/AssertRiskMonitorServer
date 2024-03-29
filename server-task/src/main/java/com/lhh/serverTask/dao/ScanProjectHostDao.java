package com.lhh.serverTask.dao;

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

    void updateEndScanDomain(@Param("domain") String domain);

    List<ScanProjectHostEntity> queryProjectByParent(@Param("parentDomain") String parentDomain);

    List<Long> queryProjectIdByParentDomain(@Param("parentDomain") String parentDomain);

    List<ScanProjectHostEntity> queryByHostList(@Param("hostList") List<String> hostList);

}
