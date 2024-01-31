package com.lhh.serverscanhole.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-09-12 15:41:27
 */
public interface ScanSecurityHoleService extends IService<ScanSecurityHoleEntity> {

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    IPage<ScanSecurityHoleEntity> page(Map<String, Object> params);

    /**
     * 根据参数查询列表
     *
     * @param params
     * @return
     */
    List<ScanSecurityHoleEntity> list(Map<String, Object> params);

    List<ScanSecurityHoleEntity> basicList(Map<String, Object> params);

}
