package com.lhh.serverinfocommon.service.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.dto.HoleNumDto;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;
import com.lhh.serverbase.vo.ScanHoleVo;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-09-12 15:41:27
 */
public interface ScanSecurityHoleService extends IService<ScanSecurityHoleEntity> {

    /**
     * 分页查询列表数据
     *
     * @param params
     * @return
     */
    IPage<ScanSecurityHoleEntity> page(Map<String, Object> params);

    List<ScanHoleVo> exportList(Map<String, Object> params);

    Integer exportNum(Map<String, Object> params);

    /**
     * 根据参数查询列表
     *
     * @param params
     * @return
     */
    List<ScanSecurityHoleEntity> list(Map<String, Object> params);

    List<ScanSecurityHoleEntity> basicList(Map<String, Object> params);

    HomeNumDto queryHomeNum(Map<String, Object> params);

    List<HoleNumDto> queryHoleNum(Map<String, Object> params);

}

