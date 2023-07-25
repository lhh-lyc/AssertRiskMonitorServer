package com.lhh.servermonitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.HostCompanyEntity;
import com.lhh.servermonitor.service.HostCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-06-25 15:49:24
 */
@RestController
@RequestMapping("hostcompany")
public class HostCompanyController {
    @Autowired
    private HostCompanyService hostCompanyService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody HostCompanyEntity hostCompany) {
        hostCompanyService.save(hostCompany);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<HostCompanyEntity> hostCompanyList) {
            hostCompanyService.saveBatch(hostCompanyList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    public void update(@RequestBody HostCompanyEntity hostCompany) {
            hostCompanyService.updateById(hostCompany);
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @PostMapping("delete")
    public void delete(Long id) {
        if (id != null) {
                hostCompanyService.removeById(id);
        }
    }

    /**
     * 批量删除
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
            hostCompanyService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<HostCompanyEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<HostCompanyEntity> hostCompanyList = (List<HostCompanyEntity>) hostCompanyService.listByMap(params);
        return hostCompanyList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<HostCompanyEntity> page(@RequestParam Map<String, Object> params) {
        IPage<HostCompanyEntity> page = hostCompanyService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<HostCompanyEntity> list(@RequestParam Map<String, Object> params) {
        List<HostCompanyEntity> hostCompanyList = hostCompanyService.list(params);
        return hostCompanyList;
    }

    @GetMapping("info")
    public HostCompanyEntity getInfo(Long id) {
            HostCompanyEntity hostCompany = hostCompanyService.getById(id);
        return hostCompany;
    }

}

