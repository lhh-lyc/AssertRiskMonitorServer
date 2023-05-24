package com.lhh.serverinfocommon.controller.scan;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.entity.ScanProjectContentEntity;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.serverinfocommon.service.scan.ScanProjectContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 项目_host角色表
 *
 * @author lyc
 * @date 2023-03-06 19:24:41
 */
@RestController
@RequestMapping("scan/project/content")
public class ScanProjectContentController {
    @Autowired
    private ScanProjectContentService scanProjectContentService;

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("save")
    public void save(@RequestBody ScanProjectContentEntity scanProjectHost) {
        scanProjectContentService.save(scanProjectHost);
    }

    /**
     * 批量保存
     *
     * @return
     */
    @PostMapping("saveBatch")
    public void save(@RequestBody List<ScanProjectContentEntity> scanProjectHostList) {
        scanProjectContentService.saveBatch(scanProjectHostList);
    }

    /**
     * 更新
     *
     * @return
     */
    @PostMapping("update")
    public void update(@RequestBody ScanProjectContentEntity scanProjectHost) {
        scanProjectContentService.updateById(scanProjectHost);
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
            scanProjectContentService.removeById(id);
        }
    }

    /**
     * 批量删除
     *
     * @return
     */
    @PostMapping("deleteBatch")
    public void deleteBatch(@RequestBody List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        scanProjectContentService.removeByIds(idList);
    }

    /**
     * 根据表格字段查询列表
     */
    @GetMapping("/listByMap")
    public List<ScanProjectContentEntity> listByMap(@RequestParam Map<String, Object> params) {
        List<ScanProjectContentEntity> scanProjectHostList = (List<ScanProjectContentEntity>) scanProjectContentService.listByMap(params);
        return scanProjectHostList;
    }

    /**
     * 分页查询列表
     */
    @GetMapping("/page")
    public IPage<ScanProjectContentEntity> page(@RequestParam Map<String, Object> params) {
        IPage<ScanProjectContentEntity> page = scanProjectContentService.page(params);
        return page;
    }

    /**
     * 根据条件查询列表数据
     */
    @GetMapping("list")
    public List<ScanProjectContentEntity> list(@RequestParam Map<String, Object> params) {
        List<ScanProjectContentEntity> scanProjectHostList = scanProjectContentService.list(params);
        return scanProjectHostList;
    }

    @GetMapping("info")
    public ScanProjectContentEntity getInfo(Long id) {
        ScanProjectContentEntity scanProjectHost = scanProjectContentService.getById(id);
        return scanProjectHost;
    }

    @PostMapping("getContentIpList")
    public List<ScanProjectContentEntity> getContentIpList(@RequestBody List<String> notIdList) {
        List<ScanProjectContentEntity> scanProjectHostList = scanProjectContentService.getContentIpList(notIdList);
        return scanProjectHostList;
    }

    @PostMapping("updateEndScanContent")
    public void updateEndScanContent() {
        scanProjectContentService.updateEndScanContent();
    }

    @PostMapping("test")
    public void test() {
        List<ScanProjectContentEntity> list = scanProjectContentService.list(new HashMap<>());
        String parentDomain = "";
        for (ScanProjectContentEntity content : list) {
            if (StringUtils.isEmpty(content.getParentDomain())) {
                if (RexpUtil.isIP(content.getInputHost())) {
                    content.setParentDomain(content.getInputHost());
                } else {
                    if (content.getUnknownTop().equals(Const.INTEGER_1)) {
                        content.setParentDomain(content.getInputHost());
                    } else {
                        parentDomain = RexpUtil.getMajorDomain(content.getInputHost());
                        content.setParentDomain(parentDomain);
                    }
                }
                scanProjectContentService.updateById(content);
            }
        }
    }

}

