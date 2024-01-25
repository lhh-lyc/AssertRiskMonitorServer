package com.lhh.serverscanhole.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.entity.CmsJsonEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-07-18 15:08:23
 */
@Mapper
public interface CmsJsonDao extends BaseMapper<CmsJsonEntity> {

    /**
     * 查询用户列表
     * @param params
     * @return
     */
    List<CmsJsonEntity> queryList(Map<String, Object> params);

}
