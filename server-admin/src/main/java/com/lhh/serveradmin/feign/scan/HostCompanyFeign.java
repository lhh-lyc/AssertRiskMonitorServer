package com.lhh.serveradmin.feign.scan;

import com.lhh.serverbase.entity.HostCompanyEntity;
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
 * @date 2023-06-25 15:49:24
 */
@FeignClient(name = "infocommon")
public interface HostCompanyFeign {

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("host/company/listByMap")
    List<HostCompanyEntity> listByMap(@RequestParam Map<String, Object> params);


    /**
     * 查询 列表数据
     *
     * @param
     */
    @GetMapping("host/company/list")
    List<HostCompanyEntity> list(@RequestParam Map<String, Object> params);

    /**
     * 保存
     */
    @PostMapping("host/company/save")
    void save(@RequestBody HostCompanyEntity hostCompany);

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("host/company/saveBatch")
    void saveBatch(@RequestBody List<HostCompanyEntity> HostCompanyList);

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("host/company/update")
    void update(@RequestBody HostCompanyEntity hostCompany);

    /**
     * 单个逻辑删除
     *
     * @param id
     * @return
     */
    @PostMapping("host/company/delete")
    void delete(Long id);

    /**
     * 批量逻辑删除
     *
     * @return
     */
    @PostMapping("host/company/deleteBatch")
    void deleteBatch(@RequestBody Long[] ids);


    @GetMapping("host/company/info")
    HostCompanyEntity info(@RequestParam(name = "id") Long id);

}


