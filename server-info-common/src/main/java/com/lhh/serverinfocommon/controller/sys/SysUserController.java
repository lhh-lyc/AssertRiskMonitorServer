package com.lhh.serverinfocommon.controller.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.SysUserEntity;
import com.lhh.serverinfocommon.service.sys.SysUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@RestController
@RequestMapping("sys/user")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("save")
    @ApiOperation(value = "保存")
    public void save(@RequestBody SysUserEntity sysUser) {
        sysUserService.save(sysUser);
    }

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("saveBatch")
    @ApiOperation(value = "批量保存")
    public void save(@RequestBody List<SysUserEntity> sysUserList) {
        sysUserService.saveBatch(sysUserList);
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("update")
    @ApiOperation(value = "更新")
    public void update(@RequestBody SysUserEntity sysUser) {
        sysUserService.updateById(sysUser);
    }

    /**
     * 单个删除
     *
     * @param id
     * @return
     */
    @PostMapping("delete")
    @ApiOperation(value = "单个删除")
    public void delete(Long id) {
        if (id != null) {
            sysUserService.removeById(id);
        }
    }

    /**
     * 批量删除
     *
     * @return
     */
    @PostMapping("deleteBatch")
    @ApiOperation(value = "批量删除")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
        sysUserService.removeByIds(idList);
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询列表")
    public IPage<SysUserEntity> page(@RequestParam Map<String, Object> params) {
        IPage<SysUserEntity> page = sysUserService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    @ApiOperation(value = "根据条件查询列表数据")
    public List<SysUserEntity> list(@RequestParam Map<String, Object> params) {
        List<SysUserEntity> sysUserList = sysUserService.list(params);
        return sysUserList;
    }

    @GetMapping("info")
    @ApiOperation(value = "详情")
    public SysUserEntity getInfo(Long id) {
        SysUserEntity sysUser = sysUserService.getById(id);
        return sysUser;
    }

    @GetMapping("queryByName")
    @ApiOperation(value = "详情")
    public SysUserEntity queryByName(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return null;
        }
        SysUserEntity user = sysUserService.queryByName(userName);
        return user;
    }

}

