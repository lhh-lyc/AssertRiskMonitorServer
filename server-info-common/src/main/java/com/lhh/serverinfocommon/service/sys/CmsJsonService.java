package com.lhh.serverinfocommon.service.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.CmsJsonEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-07-18 15:08:23
 */
public interface CmsJsonService extends IService<CmsJsonEntity> {

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    IPage<CmsJsonEntity> page(Map<String, Object> params);

    /**
     * 根据参数查询列表
     *
     * @param params
     * @return
     */
    List<CmsJsonEntity> list(Map<String, Object> params);

    void clearAll();

}

