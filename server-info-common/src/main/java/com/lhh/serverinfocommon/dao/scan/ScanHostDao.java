package com.lhh.serverinfocommon.dao.scan;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhh.serverbase.dto.KeyValueDto;
import com.lhh.serverbase.dto.ScanResultDto;
import com.lhh.serverbase.entity.ScanHostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-02-23 19:21:08
 */
@Mapper
public interface ScanHostDao extends BaseMapper<ScanHostEntity> {

    /**
    * 分页查询用户列表
    * @param page
    * @param params
    * @return
    */
    IPage<ScanHostEntity> queryPage(Page page, @Param("params") Map<String, Object> params);

    /**
     * 查询用户列表
     * @param params
     * @return
     */
    List<ScanHostEntity> queryList(Map<String, Object> params);

    List<ScanResultDto> queryDomainGroupList(Map<String, Object> params);

    Integer getCompanyNum(Map<String, Object> params);

    Integer getDomainNum(Map<String, Object> params);

    Integer getSubDomainNum(Map<String, Object> params);

    List<KeyValueDto> companyRanking(Map<String, Object> params);

    List<String> getParentDomainList(Map<String, Object> params);

    void deleteByTag(@Param("tagList") List<String> tagList, @Param("tagValueList") List<String> tagValueList);

}
