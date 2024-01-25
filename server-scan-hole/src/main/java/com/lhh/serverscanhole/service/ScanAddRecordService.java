package com.lhh.serverscanhole.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanAddRecordEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-02-23 19:21:08
 */
public interface ScanAddRecordService extends IService<ScanAddRecordEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<ScanAddRecordEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<ScanAddRecordEntity> list(Map<String, Object> params);

    void saveBatch(List<ScanAddRecordEntity> list);

}

