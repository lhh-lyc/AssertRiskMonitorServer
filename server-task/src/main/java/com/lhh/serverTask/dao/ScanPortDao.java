package com.lhh.serverTask.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lhh.serverbase.entity.ScanPortEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-02-23 19:21:07
 */
@Mapper
public interface ScanPortDao extends BaseMapper<ScanPortEntity> {

    List<ScanPortEntity> queryList(@Param("ipLong") Long ipLong);

    List<ScanPortEntity> basicByIpList(@Param("ipList") List<Long> ipList);

    void saveBatch(@Param("list") List<ScanPortEntity> list);

    void deleteBatch(@Param("idList") List<Long> idList);

    List<Integer> queryDomainPortList(@Param("domain") String domain);

}
