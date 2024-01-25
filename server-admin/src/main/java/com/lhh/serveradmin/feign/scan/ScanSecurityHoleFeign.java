package com.lhh.serveradmin.feign.scan;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.dto.HoleNumDto;
import com.lhh.serverbase.dto.HomeNumDto;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;
import com.lhh.serverbase.vo.ScanHoleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


/**
 * 系统_用户表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2023-09-12 15:41:27
 */
@FeignClient(name = "infocommon")
public interface ScanSecurityHoleFeign {

    /**
     * 查询 分页数据
     *
     * @param
     */
    @GetMapping("scan/security/hole/page")
    IPage<ScanSecurityHoleEntity> page(@RequestParam Map<String, Object> params);

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("scan/security/hole/listByMap")
    List<ScanSecurityHoleEntity> listByMap(@RequestParam Map<String, Object> params);


    /**
     * 查询 列表数据
     *
     * @param
     */
    @GetMapping("scan/security/hole/list")
    List<ScanSecurityHoleEntity> list(@RequestParam Map<String, Object> params);

    /**
     * 保存
     */
    @PostMapping("scan/security/hole/save")
    void save(@RequestBody ScanSecurityHoleEntity scanSecurityHole);

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("scan/security/hole/saveBatch")
    void saveBatch(@RequestBody List<ScanSecurityHoleEntity> ScanSecurityHoleList);

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("scan/security/hole/update")
    void update(@RequestBody ScanSecurityHoleEntity scanSecurityHole);

    /**
     * 单个逻辑删除
     *
     * @param id
     * @return
     */
    @PostMapping("scan/security/hole/delete")
    void delete(Long id);

    /**
     * 批量逻辑删除
     *
     * @return
     */
    @PostMapping("scan/security/hole/deleteBatch")
    void deleteBatch(@RequestBody List<Long> ids);


    @GetMapping("scan/security/hole/info")
    ScanSecurityHoleEntity info(@RequestParam(name = "id") Long id);

    @GetMapping("/scan/security/hole/exportList")
    List<ScanHoleVo> exportList(@RequestParam Map<String, Object> params);

    @GetMapping("scan/security/hole/exportNum")
    Integer exportNum(@RequestParam Map<String, Object> params);

    @GetMapping("/scan/security/hole/getHomeNum")
    HomeNumDto getHomeNum(@RequestParam Map<String, Object> params);

    @PostMapping("/scan/security/hole/queryHoleNum")
    List<HoleNumDto> queryHoleNum(@RequestBody Map<String, Object> params);

}


