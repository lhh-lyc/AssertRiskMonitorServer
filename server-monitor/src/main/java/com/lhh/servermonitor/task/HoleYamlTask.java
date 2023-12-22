package com.lhh.servermonitor.task;

import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.HoleYamlEntity;
import com.lhh.serverbase.utils.DateUtils;
import com.lhh.servermonitor.service.HoleYamlService;
import com.lhh.servermonitor.utils.MinioUtils;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class HoleYamlTask {

    @Value("${my-config.file.nuclei-folder}")
    private String nucleiFolder;
    @Value("${my-config.file.afrog-folder}")
    private String afrogFolder;
    @Value("${my-config.file.xray-folder}")
    private String xrayFolder;
    @Autowired
    HoleYamlService holeYamlService;
    @Autowired
    MinioUtils minioUtils;

    /**
     * 获取finger匹配的favicon hash值
     * @return
     */
    @Scheduled(cron = "0 0/20 * * * ? ")
    @GetMapping("holeYamlJson")
    public R holeYamlJson() {
        log.info("yaml漏洞规则上传定时任务开始");
        Map<String, Object> params = new HashMap<>();
        params.put("createTime", DateUtils.getYMDHms(DateUtils.addDateHours(new Date(), -1)));
        try {
            List<HoleYamlEntity> yamlList = holeYamlService.list(params);
            List<HoleYamlEntity> emptyList = yamlList.stream().filter(y->y.getToolType() == null).collect(Collectors.toList());
            List<HoleYamlEntity> nucleiList = yamlList.stream().filter(y->Const.INTEGER_1.equals(y.getToolType())).collect(Collectors.toList());
            nucleiList.addAll(emptyList);
            List<HoleYamlEntity> afrogList = yamlList.stream().filter(y->Const.INTEGER_2.equals(y.getToolType())).collect(Collectors.toList());
            afrogList.addAll(emptyList);
            List<HoleYamlEntity> xrayList = yamlList.stream().filter(y->Const.INTEGER_3.equals(y.getToolType())).collect(Collectors.toList());
            xrayList.addAll(emptyList);
            upload(nucleiList, nucleiFolder);
            upload(afrogList, afrogFolder);
            upload(xrayList, xrayFolder);
        } catch (MinioException e) {
            log.error("yaml漏洞规则上传定时任务报错", e);
        } catch (IOException e) {
            log.error("yaml漏洞规则上传定时任务报错", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("yaml漏洞规则上传定时任务报错", e);
        } catch (InvalidKeyException e) {
            log.error("yaml漏洞规则上传定时任务报错", e);
        } catch (XmlPullParserException e) {
            log.error("yaml漏洞规则上传定时任务报错", e);
        }
        log.info("yaml漏洞规则上传定时任务结束");
        return R.ok();
    }

    public void upload(List<HoleYamlEntity> yamlList, String folder) throws MinioException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        if (!CollectionUtils.isEmpty(yamlList)) {
            for (HoleYamlEntity yaml : yamlList) {
                String [] folderList = yaml.getFileUrl().split(Const.STR_SLASH);
                List<String> newFolderList = new ArrayList<>();
                for (int i = 1; i < folderList.length; i++) {
                    newFolderList.add(folderList[i]);
                }
                String newPath = String.join(Const.STR_SLASH, newFolderList);
                String path = newPath.replace(yaml.getFileName(), Const.STR_EMPTY);
                path.split(Const.STR_SLASH);
                mkdir(folder + Const.STR_SLASH + path);
                String fileUrl = Const.STR_CUSTOM + yaml.getFileUrl();
                minioUtils.uploadFileToTarget(yaml.getBucketName(), fileUrl, yaml.getFileName(), folder + Const.STR_SLASH + path);
            }
        }
    }

    public void mkdir(String path){
        File folder = new File(path);
        if (folder.exists()) {
            return;
        }
        String[] arr = path.split(Const.STR_SLASH);
        if (arr!=null && arr.length!=0) {
            String pre = arr[0];
            for (int i =1; i < arr.length; i++) {
                mkdirFor(pre, arr[i]);
                if (i!=arr.length) {
                    pre += Const.STR_SLASH + arr[i];
                }
            }
        }
    }

    public void mkdirFor(String pre, String dir){
        File folder = new File(pre + Const.STR_SLASH + dir);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

}
