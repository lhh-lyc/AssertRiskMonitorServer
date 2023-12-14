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

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class HoleYamlTask {

    @Value("${my-config.file.target}")
    private String target;
    @Autowired
    HoleYamlService holeYamlService;
    @Autowired
    MinioUtils minioUtils;

    /**
     * 获取finger匹配的favicon hash值
     * @return
     */
    @Scheduled(cron = "0 0/30 * * * ? ")
    @GetMapping("holeYamlJson")
    public R holeYamlJson() {
        log.info("yaml漏洞规则上传定时任务开始");
        Map<String, Object> params = new HashMap<>();
        params.put("createTime", DateUtils.getYMDHms(DateUtils.addDateHours(new Date(), -1)));
        try {
            List<HoleYamlEntity> yamlList = holeYamlService.list(params);
            if (!CollectionUtils.isEmpty(yamlList)) {
                for (HoleYamlEntity yaml : yamlList) {
                    String fileUrl = yaml.getFileUrl();
                    String foldName = fileUrl.split(Const.STR_SLASH)[0];
                    String objectName = fileUrl.split(Const.STR_SLASH)[1];
                    minioUtils.uploadFileToTarget(yaml.getBucketName(), foldName, objectName, yaml.getFileName(), target);
                }
            }
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

}
