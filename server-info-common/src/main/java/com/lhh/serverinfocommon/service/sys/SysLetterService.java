package com.lhh.serverinfocommon.service.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.SysLetterEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-06-11 12:18:45
 */
public interface SysLetterService extends IService<SysLetterEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<SysLetterEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<SysLetterEntity> list(Map<String, Object> params);

    void readByUserId(Long userId);

    Integer unReadNum(Long userId);

}

