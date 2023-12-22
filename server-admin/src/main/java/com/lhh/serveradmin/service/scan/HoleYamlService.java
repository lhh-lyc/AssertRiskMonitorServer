package com.lhh.serveradmin.service.scan;

import com.lhh.serveradmin.feign.scan.HoleYamlFeign;
import com.lhh.serveradmin.feign.scan.ScanSecurityHoleFeign;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serveradmin.service.FileService;
import com.lhh.serveradmin.utils.ExecUtil;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.dto.FileInfoDTO;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
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
    @Autowired
    private PassJavaJwtTokenUtil jwtTokenUtil;

    /**
     * 查询 分页数据
     *
     * @param
     */
    public R page(Map<String, Object> params) {
        IPage<HoleYamlEntity> page = holeYamlFeign.page(params);
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            for (HoleYamlEntity yaml : page.getRecords()) {
                String [] folderList = yaml.getFileUrl().split(Const.STR_SLASH);
                List<String> newFolderList = new ArrayList<>();
                for (int j = 2; j < folderList.length; j++) {
                    newFolderList.add(folderList[j]);
                }
                String newPath = Const.STR_SLASH + String.join(Const.STR_SLASH, newFolderList);
                String path = newPath.replace(yaml.getFileName(), Const.STR_EMPTY);
                yaml.setFilePath(path);
            }
        }
        return R.ok(page);
    }

    public R uploadFiles(List<MultipartFile> files, List<String> paths, Integer toolType){
        List<HoleYamlEntity> saveList = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String path = paths.get(i);
            String [] folderList = path.split(Const.STR_SLASH);
            List<String> newFolderList = new ArrayList<>();
            for (int j = 1; j < folderList.length; j++) {
                newFolderList.add(folderList[j]);
            }
            String newPath = Const.STR_SLASH + String.join(Const.STR_SLASH, newFolderList);
            FileInfoDTO dto = null;
            String[] arr = file.getOriginalFilename().split(Const.STR_SLASH);
            String fileName = arr[arr.length-1];
            String folder = newPath.replace(Const.STR_SLASH + fileName, Const.STR_EMPTY);
            try {
                dto = fileService.uploadFile(defBucket, file.getInputStream(), fileName, Const.STR_CUSTOM + folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Long userId = Long.valueOf(jwtTokenUtil.getUserId());
            HoleYamlEntity yaml = HoleYamlEntity.builder()
                    .bucketName(dto.getBucketName()).toolType(toolType)
                    .fileName(dto.getFileOrgName()).fileUrl(dto.getFileUrl())
                    .fileType(dto.getFileType()).userId(userId)
                    .build();
            yaml.setCreateTime(new Date());
            yaml.setUpdateTime(new Date());
            yaml.setDelFlg(Const.INTEGER_0);
            saveList.add(yaml);
        }
        holeYamlFeign.saveBatch(saveList);
        return R.ok();
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

    public R downYaml(List<String> fileUrlList) {
        List<String> urlList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(fileUrlList)) {
            for (String fileUrl : fileUrlList) {
                String url = fileService.download(defBucket, fileUrl);
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
                    String fileUrl = yaml.getFileUrl();
                    if ((nucleiFolder + Const.STR_SLASH + fileUrl).contains("/*") ||
                            (afrogFolder + Const.STR_SLASH + fileUrl).contains("/*") ||
                            (xrayFolder + Const.STR_SLASH + fileUrl).contains("/*")) {
                        continue;
                    }
                    if (Const.INTEGER_1.equals(yaml.getToolType())) {
                        String cmd = String.format(Const.STR_DEL_HOLE_YAML, nucleiFolder + fileUrl);
                        ExecUtil.runCommand(cmd);
                    }
                    if (Const.INTEGER_2.equals(yaml.getToolType())) {
                        String cmd = String.format(Const.STR_DEL_HOLE_YAML, afrogFolder + fileUrl);
                        ExecUtil.runCommand(cmd);
                    }
                    if (Const.INTEGER_3.equals(yaml.getToolType())) {
                        String cmd = String.format(Const.STR_DEL_HOLE_YAML, xrayFolder + fileUrl);
                        ExecUtil.runCommand(cmd);
                    }
                    if(yaml.getToolType() == null) {
                        String cmd = String.format(Const.STR_DEL_HOLE_YAML, nucleiFolder + fileUrl);
                        ExecUtil.runCommand(cmd);
                        cmd = String.format(Const.STR_DEL_HOLE_YAML, afrogFolder + fileUrl);
                        ExecUtil.runCommand(cmd);
                        cmd = String.format(Const.STR_DEL_HOLE_YAML, xrayFolder + fileUrl);
                        ExecUtil.runCommand(cmd);
                    }
                }
            }
            List<String> fileUrlList = list.stream().map(HoleYamlEntity::getFileUrl).collect(Collectors.toList());
            fileService.deleteFile(defBucket, fileUrlList);
            holeYamlFeign.deleteBatch(ids);
        } catch (IOException e) {
            log.error("删除规则出错", e);
        }
        return R.ok();
    }

}



