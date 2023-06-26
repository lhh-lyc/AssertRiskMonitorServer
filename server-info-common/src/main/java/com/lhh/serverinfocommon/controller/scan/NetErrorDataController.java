package com.lhh.serverinfocommon.controller.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lhh.serverbase.entity.NetErrorDataEntity;
import com.lhh.serverinfocommon.service.scan.NetErrorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
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
@RequestMapping("net/error/data")
public class NetErrorDataController {
    @Autowired
    private NetErrorDataService netErrorDataService;

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody NetErrorDataEntity netErrorData) {
        netErrorDataService.save(netErrorData);
    }

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<NetErrorDataEntity> netErrorDataList) {
        netErrorDataService.saveBatch(netErrorDataList);
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("update")
    public void update(@RequestBody NetErrorDataEntity netErrorData) {
        netErrorDataService.updateById(netErrorData);
    }

    /**
     * 单个删除
     *
     * @param id
     * @return
     */
    @PostMapping("delete")
    public void delete(Long id) {
        if (id != null) {
            netErrorDataService.removeById(id);
        }
    }

    /**
     * 批量删除
     *
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        netErrorDataService.removeByIds(ids);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<NetErrorDataEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<NetErrorDataEntity> netErrorDataList = (List<NetErrorDataEntity>) netErrorDataService.listByMap(params);
        return netErrorDataList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<NetErrorDataEntity> page(@RequestParam Map<String, Object> params) {
        IPage<NetErrorDataEntity> page = netErrorDataService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<NetErrorDataEntity> list(@RequestParam Map<String, Object> params) {
        List<NetErrorDataEntity> netErrorDataList = netErrorDataService.list(params);
        return netErrorDataList;
    }

    @GetMapping("info")
    public NetErrorDataEntity getInfo(Long id) {
        NetErrorDataEntity netErrorData = netErrorDataService.getById(id);
        return netErrorData;
    }

}

