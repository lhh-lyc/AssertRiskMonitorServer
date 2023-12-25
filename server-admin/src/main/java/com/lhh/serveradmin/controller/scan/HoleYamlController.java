package com.lhh.serveradmin.controller.scan;

import com.lhh.serveradmin.service.scan.HoleYamlService;
import com.lhh.serveradmin.utils.MinioUtils;
import com.lhh.serverbase.common.response.R;
import io.minio.errors.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("hole/yaml")
public class HoleYamlController {

    @Autowired
    HoleYamlService holeYamlService;

    @GetMapping("page")
    public R page(@RequestParam Map<String, Object> params){
        return R.ok(holeYamlService.page(params));
    }

    @PostMapping("uploadFiles")
    public R uploadFiles(@RequestParam("files") List<MultipartFile> files,
                         @RequestParam("paths") List<String> paths,
                         @RequestParam("toolType") Integer toolType,
                         @RequestParam("folderId") Long folderId){
        if (CollectionUtils.isEmpty(files) || CollectionUtils.isEmpty(paths)) {
            return R.failed("文件不能为空");
        }
        holeYamlService.uploadFiles(files, paths, toolType, folderId);
        return R.ok();
    }

    @PostMapping("downYaml")
    public R downYaml(@RequestBody List<String> fileUrlList){
        return holeYamlService.downYaml(fileUrlList);
    }

    /**
     * 单个删除
     * @param ids
     * @return
     */
    @PostMapping("delete")
    public R delete(@RequestBody List<Long> ids) {
        return holeYamlService.delete(ids);
    }

    @Autowired
    MinioUtils minioUtils;
    @GetMapping("del")
    public R del() {
        try {
            minioUtils.deleteFile("assertfiles", "/custom/3/test3.yaml");
        } catch (InvalidPortException e) {
            e.printStackTrace();
        } catch (InvalidEndpointException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoResponseException e) {
            e.printStackTrace();
        } catch (InvalidBucketNameException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (InvalidResponseException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return R.ok();
    }

}
