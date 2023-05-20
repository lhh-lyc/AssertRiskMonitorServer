package com.lhh.servermonitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.entity.ScanHostEntity;
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
public interface ScanHostDao extends BaseMapper<ScanHostEntity> {

    void updateScanPorts(@Param("list") List<ScanHostEntity> list);

    void updateEndScanIp(@Param("ipLong") Long ipLong);

    /**
     * 查询host表ip对应的子域名，是否分别都扫描完成
     * @param ipLong
     * @return
     */
    List<String> getEndScanDomain(@Param("ipLong") Long ipLong);


    /**
     * 查询host表ip对应的主域名，是否分别都扫描完成
     * @param ipLong
     * @return
     */
    List<String> getEndScanMajor(@Param("ipLong") Long ipLong);

}
