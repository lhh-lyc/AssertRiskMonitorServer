package com.lhh.serverTask.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanHostEntity;

import java.util.List;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-02-23 19:21:08
 */
public interface ScanHostService extends IService<ScanHostEntity> {

    List<String> getParentList();

}

