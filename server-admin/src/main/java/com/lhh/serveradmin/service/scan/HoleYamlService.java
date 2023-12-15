package com.lhh.serveradmin.service.scan;

import com.lhh.serveradmin.feign.scan.HoleYamlFeign;
import com.lhh.serveradmin.feign.scan.ScanSecurityHoleFeign;
import com.lhh.serveradmin.service.FileService;
import com.lhh.serveradmin.utils.ExecUtil;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.HoleYamlEntity;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;
import com.lhh.serverbase.entity.SshResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 系统_用户表应用服务层
 *
 * @author lyc
 * @date 2023-09-12 15:41:27
 */
@Slf4j
@Service
public class HoleYamlService {

    @Value("${my-config.upload.defBucket}")
    private String defBucket;
    @Value("${my-config.file.nuclei-folder}")
    private String nucleiFolder;
    @Value("${my-config.file.afrog-folder}")
    private String afrogFolder;
    @Value("${my-config.file.xray-folder}")
    private String xrayFolder;
    @Autowired
    private HoleYamlFeign holeYamlFeign;
    @Autowired
    FileService fileService;

    /**
     * 查询 分页数据
     *
     * @param
     */
    public R page(Map<String, Object> params) {
        IPage<HoleYamlEntity> page = holeYamlFeign.page(params);
        return R.ok(page);
    }

    /**
     * 查询 列表数据
     *
     * @param
     */
    public R list(Map<String, Object> params) {
        List<HoleYamlEntity> list = holeYamlFeign.list(params);
        return R.ok(list);
    }

    public R downYaml(List<String> fileNameList) {
        List<String> urlList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(fileNameList)) {
            for (String fileName : fileNameList) {
                String url = fileService.download(defBucket, fileName);
                urlList.add(url);
            }
        }
        return R.ok(urlList);
    }

    /**
     * 批量逻辑删除
     *
     * @return
     */
    public R delete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return R.error("请选择删除数据");
        }
        List<HoleYamlEntity> list = holeYamlFeign.list(new HashMap<String, Object>(){{put("idList", ids);}});
        try {
            if (!CollectionUtils.isEmpty(list)) {
                for (HoleYamlEntity yaml : list) {
                    String name = yaml.getFileName();
                    if ((nucleiFolder + Const.STR_SLASH + name).contains("/*") ||
                            (afrogFolder + Const.STR_SLASH + name).contains("/*") ||
                            (xrayFolder + Const.STR_SLASH + name).contains("/*")) {
                        continue;
                    }
                    if (Const.INTEGER_1.equals(yaml.getToolType())) {
                        String cmd = String.format(Const.STR_DEL_HOLE_YAML, nucleiFolder + Const.STR_SLASH + name);
                        ExecUtil.runCommand(cmd);
                    }
                    if (Const.INTEGER_2.equals(yaml.getToolType())) {
                        String cmd = String.format(Const.STR_DEL_HOLE_YAML, afrogFolder + Const.STR_SLASH + name);
                        ExecUtil.runCommand(cmd);
                    }
                    if (Const.INTEGER_3.equals(yaml.getToolType())) {
                        String cmd = String.format(Const.STR_DEL_HOLE_YAML, xrayFolder + Const.STR_SLASH + name);
                        ExecUtil.runCommand(cmd);
                    }
                    if(yaml.getToolType() == null) {
                        String cmd = String.format(Const.STR_DEL_HOLE_YAML, nucleiFolder + Const.STR_SLASH + name);
                        ExecUtil.runCommand(cmd);
                        cmd = String.format(Const.STR_DEL_HOLE_YAML, afrogFolder + Const.STR_SLASH + name);
                        ExecUtil.runCommand(cmd);
                        cmd = String.format(Const.STR_DEL_HOLE_YAML, xrayFolder + Const.STR_SLASH + name);
                        ExecUtil.runCommand(cmd);
                    }
                }
            }
        } catch (IOException e) {
            log.error("删除规则出错", e);
        }
        holeYamlFeign.deleteBatch(ids);
        return R.ok();
    }

}



