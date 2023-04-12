package com.lhh.serverinfocommon.service.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanProjectContentEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户角色表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-03-06 19:24:41
 */
public interface ScanProjectContentService extends IService<ScanProjectContentEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<ScanProjectContentEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<ScanProjectContentEntity> list(Map<String, Object> params);

}

