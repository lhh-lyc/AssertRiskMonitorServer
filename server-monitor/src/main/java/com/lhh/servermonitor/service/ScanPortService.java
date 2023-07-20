package com.lhh.servermonitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanPortEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-02-23 19:21:07
 */
public interface ScanPortService extends IService<ScanPortEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<ScanPortEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<ScanPortEntity> list(Map<String, Object> params);

    List<Integer> queryDomainPortList(String domain);

    List<ScanPortEntity> basicList(Map<String, Object> params);

    List<ScanPortEntity> basicByIpList(List<Long> ipList);

    void saveBatch(List<ScanPortEntity> list);

}

