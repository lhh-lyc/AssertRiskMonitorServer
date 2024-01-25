package com.lhh.serverscanhole.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.entity.ScanHostPortEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-07-12 15:38:11
 */
@Mapper
public interface ScanHostPortDao extends BaseMapper<ScanHostPortEntity> {

    /**
    * 分页查询用户列表
    * @param page
    * @param params
    * @return
    */
    IPage<ScanHostPortEntity> queryPage(Page page, @Param("params") Map<String, Object> params);

    List<ScanHostPortEntity> queryList(Map<String, Object> params);

    void saveBatch(@Param("list") List<ScanHostPortEntity> list);

    void delByDomain(@Param("domain") String domain);

}
