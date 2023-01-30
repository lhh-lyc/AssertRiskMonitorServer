package com.lhh.serverinfocommon.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.SysMenuEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_菜单表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2022-12-28 14:21:23
 */
public interface SysMenuService extends IService<SysMenuEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<SysMenuEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<SysMenuEntity> list(Map<String, Object> params);

}

