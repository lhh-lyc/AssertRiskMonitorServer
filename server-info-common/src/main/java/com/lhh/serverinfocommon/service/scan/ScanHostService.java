package com.lhh.serverinfocommon.service.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.dto.KeyValueDto;
import com.lhh.serverbase.dto.ScanResultDto;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.serverbase.entity.ScanHostEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-02-23 19:21:08
 */
public interface ScanHostService extends IService<ScanHostEntity> {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    IPage<ScanHostEntity> page(Map<String, Object> params);

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<ScanHostEntity> list(Map<String, Object> params);

    List<ScanHostEntity> getByDomainList(List<String> hostList);

    List<ScanHostEntity> getByIpList(List<Long> hostList);

    List<ScanResultDto> queryDomainGroupList(Map<String, Object> params);

    List<ScanHostEntity> equalParams(Map<String, Object> params);

    Integer getCompanyNum(Map<String, Object> params);

    Integer getDomainNum(Map<String, Object> params);

    Integer getSubDomainNum(Map<String, Object> params);

    List<HostCompanyEntity> getParentDomainList(Map<String, Object> params);

    List<KeyValueDto> companyRanking(Map<String, Object> params);

    void endScanIp(Long ipLong, String scanPorts);

    List<String> getParentList();

}

