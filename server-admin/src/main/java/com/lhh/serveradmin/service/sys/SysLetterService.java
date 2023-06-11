package com.lhh.serveradmin.service.sys;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cn.hutool.core.map.MapUtil;
import com.lhh.serveradmin.feign.sys.SysLetterFeign;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.SysLetterEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 系统_用户表应用服务层
 *
 * @author lyc
 * @date 2023-06-11 12:18:45
 */
@Service
public class SysLetterService {

    @Autowired
    private SysLetterFeign sysLetterFeign;
    @Resource
    private PassJavaJwtTokenUtil jwtTokenUtil;

    /**
     * 查询 分页数据
     *
     * @param
     */
    public R page(Map<String, Object> params) {
        IPage<SysLetterEntity> page = sysLetterFeign.page(params);
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            for (SysLetterEntity letter : page.getRecords()) {
                List<String> list = Arrays.asList(letter.getContent().split(Const.STR_COMMA));
                letter.setDomainList(list);
            }
        }
        return R.ok(page);
    }

    /**
     * 根据表格字段查询列表
     */
    public R listByMap(Map<String, Object> params) {
        List<SysLetterEntity> list = sysLetterFeign.listByMap(params);
        return R.ok(list);
    }

    /**
     * 查询 列表数据
     *
     * @param
     */
    public R list(Map<String, Object> params) {
        List<SysLetterEntity> list = sysLetterFeign.list(params);
        return R.ok(list);
    }

    /**
     * 保存
     */
    public R save(@RequestBody SysLetterEntity sysLetter) {
        sysLetterFeign.save(sysLetter);
        return R.ok();
    }

    /**
     * 批量保存
     *
     * @return
     */
    public R saveBatch(@RequestBody List<SysLetterEntity> SysLetterList) {
        sysLetterFeign.saveBatch(SysLetterList);
        return R.ok();
    }

    /**
     * 更新
     *
     * @return
     */
    public R update(@RequestBody SysLetterEntity sysLetter) {
        sysLetterFeign.update(sysLetter);
        return R.ok();
    }

    /**
     * 单个逻辑删除
     *
     * @param id
     * @return
     */
    public R delete(Long id) {
        sysLetterFeign.delete(id);
        return R.ok();
    }

    /**
     * 批量逻辑删除
     *
     * @return
     */
    public R deleteBatch(Long[] ids) {
        sysLetterFeign.deleteBatch(ids);
        return R.ok();
    }

    /**
     * 查询详情
     *
     * @return
     */
    public R info(@RequestParam(name = "id") Long id) {
        SysLetterEntity sysLetter = sysLetterFeign.info(id);
        return R.ok(sysLetter);
    }

    public R read(Map<String, Object> params) {
        Long id = MapUtil.getLong(params, "id");
        if (id == null) {
            Long userId = Long.valueOf(jwtTokenUtil.getUserId());
            sysLetterFeign.readByUserId(userId);
        } else {
            SysLetterEntity letter = sysLetterFeign.info(id);
            letter.setStatus(Const.INTEGER_1);
            sysLetterFeign.update(letter);
        }
        return R.ok();
    }

    public R unReadNum() {
        Long userId = Long.valueOf(jwtTokenUtil.getUserId());
        Integer num = sysLetterFeign.unReadNum(userId);
        return R.ok(num);
    }

}



