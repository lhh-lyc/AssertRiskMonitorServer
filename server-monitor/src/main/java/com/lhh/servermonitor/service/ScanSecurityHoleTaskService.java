package com.lhh.servermonitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanSecurityHoleTaskEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户角色表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-12-28 17:49:19
 */
public interface ScanSecurityHoleTaskService extends IService<ScanSecurityHoleTaskEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<ScanSecurityHoleTaskEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<ScanSecurityHoleTaskEntity> list(Map<String, Object> params);

}

