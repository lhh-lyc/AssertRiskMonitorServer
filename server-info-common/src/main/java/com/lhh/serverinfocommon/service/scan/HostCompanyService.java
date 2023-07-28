package com.lhh.serverinfocommon.service.scan;

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

    HostCompanyEntity queryBasicInfo(String host);

}

