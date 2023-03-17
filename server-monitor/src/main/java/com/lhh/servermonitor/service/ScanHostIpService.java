package com.lhh.servermonitor.service;

import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanHostIpEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户角色表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-03-16 17:05:59
 */
public interface ScanHostIpService extends IService<ScanHostIpEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<ScanHostIpEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<ScanHostIpEntity> list(Map<String, Object> params);

}

