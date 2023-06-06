package com.lhh.servermonitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.entity.ScanProjectHostEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户角色表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-03-06 19:24:41
 */
public interface ScanProjectHostService extends IService<ScanProjectHostEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<ScanProjectHostEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<ScanProjectHostEntity> list(Map<String, Object> params);

    List<ScanProjectHostEntity> selByProIdAndHost(Long projectId, String host);

    void updateEndScanDomain(String domain);

}

