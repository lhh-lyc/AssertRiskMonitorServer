package com.lhh.serverReScan.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lhh.serverbase.entity.ScanPortEntity;
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

    List<ScanPortEntity> queryList(@Param("ip") Long ip);

    List<ScanPortEntity> basicByIpList(@Param("ipList") List<Long> ipList);

    void saveBatch(@Param("list") List<ScanPortEntity> list);

}
