package com.lhh.serverinfocommon.service.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.NetErrorDataEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-06-25 15:49:24
 */
public interface NetErrorDataService extends IService<NetErrorDataEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<NetErrorDataEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<NetErrorDataEntity> list(Map<String, Object> params);

}

