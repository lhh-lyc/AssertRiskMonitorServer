package com.lhh.servermonitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.HoleYamlEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_文件表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2022-12-28 14:21:23
 */
public interface HoleYamlService extends IService<HoleYamlEntity> {

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<HoleYamlEntity> list(Map<String, Object> params);

}

