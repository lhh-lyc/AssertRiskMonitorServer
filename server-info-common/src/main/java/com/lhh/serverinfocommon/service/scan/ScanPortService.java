package com.lhh.serverinfocommon.service.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.dto.GroupTagDto;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.vo.ScanPortVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-02-23 19:21:07
 */
public interface ScanPortService extends IService<ScanPortEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<ScanPortEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<ScanPortEntity> list(Map<String, Object> params);

    List<ScanPortEntity> getByIpList(List<String> hostList);

    HomeNumDto queryHomeNum(Map<String, Object> params);

    IPage<GroupTagDto> queryGroupTag(Map<String, Object> params);

    List<ScanPortVo> exportList(Map<String, Object> params);

    void deleteByIpPort(Map<String, Object> params);

}

