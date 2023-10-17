package com.lhh.servermonitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanPortEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
     * 查询用户列表
     * @param params
     * @return
     */
    List<ScanPortEntity> queryList(Map<String, Object> params);

    List<Integer> queryWebPortList(@Param("domain") String domain);

    List<Integer> queryPortList(@Param("domain") String domain);

    List<ScanPortEntity> basicByIpList(@Param("ipList") List<Long> ipList);

    void saveBatch(@Param("list") List<ScanPortEntity> list);

    void updateBatch(@Param("list") List<ScanPortEntity> list);

    void delBatch(@Param("idList") List<Long> list);

}
