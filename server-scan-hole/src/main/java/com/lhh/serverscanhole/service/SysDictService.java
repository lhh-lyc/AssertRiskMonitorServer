package com.lhh.serverscanhole.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.SysDictEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_字典表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2022-12-28 14:21:23
 */
public interface SysDictService extends IService<SysDictEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<SysDictEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<SysDictEntity> list(Map<String, Object> params);

}

