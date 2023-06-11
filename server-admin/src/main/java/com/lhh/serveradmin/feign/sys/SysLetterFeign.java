package com.lhh.serveradmin.feign.sys;

import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.SysLetterEntity;
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
 * @date 2023-06-11 12:18:45
 */
@FeignClient(name = "infocommon")
public interface SysLetterFeign {

    /**
     * 查询 分页数据
     *
     * @param
     */
    @GetMapping("sys/letter/page")
    IPage<SysLetterEntity> page(@RequestParam Map<String, Object> params);

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("sys/letter/listByMap")
    List<SysLetterEntity> listByMap(@RequestParam Map<String, Object> params);


    /**
     * 查询 列表数据
     *
     * @param
     */
    @GetMapping("sys/letter/list")
    List<SysLetterEntity> list(@RequestParam Map<String, Object> params);

    /**
     * 保存
     */
    @PostMapping("sys/letter/save")
    void save(@RequestBody SysLetterEntity sysLetter);

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("sys/letter/saveBatch")
    void saveBatch(@RequestBody List<SysLetterEntity> SysLetterList);

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("sys/letter/update")
    void update(@RequestBody SysLetterEntity sysLetter);

    /**
     * 单个逻辑删除
     *
     * @param id
     * @return
     */
    @PostMapping("sys/letter/delete")
    void delete(Long id);

    /**
     * 批量逻辑删除
     *
     * @return
     */
    @PostMapping("sys/letter/deleteBatch")
    void deleteBatch(@RequestBody Long[] ids);

    @GetMapping("sys/letter/info")
    SysLetterEntity info(@RequestParam(name = "id") Long id);

    /**
     * 单个逻辑删除
     *
     * @param userId
     * @return
     */
    @PostMapping("sys/letter/readByUserId")
    void readByUserId(@RequestParam("userId") Long userId);

    @GetMapping("sys/letter/unReadNum")
    Integer unReadNum(@RequestParam(name = "userId") Long userId);

}


