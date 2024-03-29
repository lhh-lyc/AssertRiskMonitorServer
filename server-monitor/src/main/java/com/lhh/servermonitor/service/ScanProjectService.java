package com.lhh.servermonitor.service;

import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanProjectEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-02-23 19:21:08
 */
public interface ScanProjectService extends IService<ScanProjectEntity> {

    void sendToMqtt(ScanProjectEntity project);

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<ScanProjectEntity> page(Map<String, Object> params);

    void saveProject(ScanProjectEntity project);

    List<ScanProjectEntity> getByNameAndUserId(Long userId, String name);

}

