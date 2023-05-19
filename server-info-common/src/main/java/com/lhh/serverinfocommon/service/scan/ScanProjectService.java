package com.lhh.serverinfocommon.service.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.ScanProjectEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-02-23 19:21:08
 */
public interface ScanProjectService extends IService<ScanProjectEntity> {

    IPage<ScanProjectEntity> basicPage(Map<String, Object> params);

    List<ScanProjectEntity> getProjectPortNum(List<Long> projectIdList);

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<ScanProjectEntity> page(Map<String, Object> params);

    List<ScanProjectEntity> l(Map<String, Object> params);

    /**
     * 根据参数查询列表
     * @param params
     * @return
     */
    List<ScanProjectEntity> list(Map<String, Object> params);

}

