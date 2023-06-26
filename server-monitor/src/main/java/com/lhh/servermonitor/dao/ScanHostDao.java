package com.lhh.servermonitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanPortEntity;
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
public interface ScanHostDao extends BaseMapper<ScanHostEntity> {

    void updateEndScanDomain(@Param("domain") String domain);

    void updateEndScanIp(@Param("ipLong") Long ipLong, @Param("scanPorts") String scanPorts);

    void returnScanStatus(@Param("ipLong") Long ipLong);

    List<ScanHostEntity> getByParentDomainList(@Param("hostList") List<String> hostList);

    /**
     * @param ipLongList
     * @return
     */
    List<ScanHostEntity> getByIpList(@Param("ipLongList") List<Long> ipLongList, @Param("domain") String domain);

    List<ScanHostEntity> getIpByIpList(@Param("ipLongList") List<Long> ipLongList);

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

    void saveBatch(@Param("list") List<ScanHostEntity> list);

    List<ScanHostEntity> basicList(Map<String, Object> params);

}
