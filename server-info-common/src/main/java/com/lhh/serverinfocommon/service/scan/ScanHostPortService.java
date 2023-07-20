package com.lhh.serverinfocommon.service.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanHostPortEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-07-12 15:38:11
 */
public interface ScanHostPortService extends IService<ScanHostPortEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<ScanHostPortEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<ScanHostPortEntity> list(Map<String, Object> params);

}

