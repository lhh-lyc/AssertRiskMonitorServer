package com.lhh.serverinfocommon.controller.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.SysLetterEntity;
import com.lhh.serverinfocommon.service.sys.SysLetterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-06-11 12:18:45
 */
@RestController
@RequestMapping("sys/letter")
public class SysLetterController {
    @Autowired
    private SysLetterService sysLetterService;

    /**
     * 保存
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody SysLetterEntity sysLetter) {
            sysLetterService.save(sysLetter);
    }

    /**
     * 批量保存
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<SysLetterEntity> sysLetterList) {
            sysLetterService.saveBatch(sysLetterList);
    }

    /**
    * 更新
    * @return
    */
    @PostMapping("update")
    public void update(@RequestBody SysLetterEntity sysLetter) {
            sysLetterService.updateById(sysLetter);
    }

    /**
     * 单个删除
     * @param id
     * @return
     */
    @PostMapping("delete")
    public void delete(Long id) {
        if (id != null) {
                sysLetterService.removeById(id);
        }
    }

    /**
     * 批量删除
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
            sysLetterService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<SysLetterEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<SysLetterEntity> sysLetterList = (List<SysLetterEntity>) sysLetterService.listByMap(params);
        return sysLetterList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<SysLetterEntity> page(@RequestParam Map<String, Object> params) {
        IPage<SysLetterEntity> page = sysLetterService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<SysLetterEntity> list(@RequestParam Map<String, Object> params) {
        List<SysLetterEntity> sysLetterList = sysLetterService.list(params);
        return sysLetterList;
    }

    @GetMapping("info")
    public SysLetterEntity getInfo(Long id) {
        SysLetterEntity sysLetter = sysLetterService.getById(id);
        return sysLetter;
    }

    @PostMapping("readByUserId")
    public void readByUserId(Long userId) {
        sysLetterService.readByUserId(userId);
    }

    @GetMapping("unReadNum")
    public Integer unReadNum(Long userId) {
        Integer num = sysLetterService.unReadNum(userId);
        return num;
    }

}

