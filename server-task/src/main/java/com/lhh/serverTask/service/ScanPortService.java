package com.lhh.serverTask.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanPortEntity;

import java.util.List;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-02-23 19:21:07
 */
public interface ScanPortService extends IService<ScanPortEntity> {

    List<ScanPortEntity> basicByIpList(List<Long> ipList);

    void deleteBatch(List<Long> idList);

    List<Integer> queryDomainPortList(String domain);

}

