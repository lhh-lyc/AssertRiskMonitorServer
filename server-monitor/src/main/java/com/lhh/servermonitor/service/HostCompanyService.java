package com.lhh.servermonitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.HostCompanyEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-06-25 15:49:24
 */
public interface HostCompanyService extends IService<HostCompanyEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<HostCompanyEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<HostCompanyEntity> list(Map<String, Object> params);

    List<HostCompanyEntity> queryByHostList(List<String> hostList);

    HostCompanyEntity queryByHost(String host);

    void updatePorts(String domain, String scanPorts);

    String getCompany(String domain);

    String getScanPorts(String domain);

    HostCompanyEntity getHostInfo(String domain);

    HostCompanyEntity setHostInfo(String domain);

    List<HostCompanyEntity> setHostInfoList(List<String> domainList);

}

