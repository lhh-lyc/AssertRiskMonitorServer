package com.lhh.serveradmin.service.scan;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.lhh.serveradmin.feign.scan.HoleYamlFeign;
import com.lhh.serveradmin.feign.scan.ScanHostFeign;
import com.lhh.serveradmin.feign.scan.ScanPortFeign;
import com.lhh.serveradmin.feign.scan.ScanSecurityHoleFeign;
import com.lhh.serveradmin.feign.sys.CmsJsonFeign;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serveradmin.mqtt.FilePushSender;
import com.lhh.serveradmin.service.FileService;
import com.lhh.serveradmin.utils.MinioUtils;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.constant.ExcelConstant;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.dto.CmsJsonDto;
import com.lhh.serverbase.dto.FileInfoDTO;
import com.lhh.serverbase.dto.FingerprintListDTO;
import com.lhh.serverbase.entity.CmsJsonEntity;
import com.lhh.serverbase.entity.HoleYamlEntity;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.enums.ExportTypeEnum;
import com.lhh.serverbase.utils.ImportExcelUtils;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.serverbase.vo.ScanHoleVo;
import com.lhh.serverbase.vo.ScanPortVo;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Value("${my-config.upload.defFolder}")
    private String defFolder;
    @Value("${my-config.upload.defBucket}")
    private String defBucket;
    @Autowired
    ScanHostFeign scanHostFeign;
    @Autowired
    ScanPortFeign scanPortFeign;
    @Autowired
    ScanSecurityHoleFeign scanSecurityHoleFeign;
    @Autowired
    FilePushSender filePushSender;
    @Autowired
    FileService fileService;
    @Autowired
    CmsJsonFeign cmsJsonFeign;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Resource
    private PassJavaJwtTokenUtil jwtTokenUtil;
    @Resource
    private HoleYamlFeign holeYamlFeign;


    public R upload(MultipartFile file) {
        Map<String, List<List<String>>> excelDataMap = ImportExcelUtils.readExcel(file, 1, 0, 0);
        List<List<String>> dataList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(excelDataMap)) {
            for (String key : excelDataMap.keySet()) {
                dataList = excelDataMap.get(key);
            }
        }
        List<String> domainList = new ArrayList<>();
        List<ScanHostEntity> hostList = new ArrayList<>();
        List<ScanPortEntity> portList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dataList)) {
            for (List<String> data : dataList) {
//                domainList.add(data.get(2));
                if (StringUtils.isEmpty(data.get(4))) {
                    continue;
                }
                ScanHostEntity host = ScanHostEntity.builder()
                        .domain(data.get(2)).parentDomain(data.get(1))
                        .ip(data.get(3)).type(data.get(2).equals(data.get(1)) ? Const.INTEGER_1 : Const.INTEGER_3)
                        .scanPorts(data.get(4)).company(data.get(0))
                        .build();
                hostList.add(host);
                ScanPortEntity port = ScanPortEntity.builder()
                        .ip(data.get(3)).port(Integer.valueOf(data.get(4)))
                        .serverName(data.get(5))
                        .build();
                portList.add(port);
            }
        }
        if (!CollectionUtils.isEmpty(hostList)) {
            scanHostFeign.saveBatch(hostList);
        }
        if (!CollectionUtils.isEmpty(portList)) {
            scanPortFeign.saveBatch(portList);
        }
        /*List<ScanHostEntity> hostList = scanHostFeign.getByDomainList(domainList);
        String parentDomain = "";
        String domain = "";
        String ip = "";
        Map<String, Object> params = new HashMap<>();
        params.put("domain", domain);
        params.put("parentDomain", parentDomain);
        params.put("ip", ip);
        List<ScanHostEntity> hostList = scanHostFeign.equalParams(params);
        if (!CollectionUtils.isEmpty(hostList)) {
            return R.error("");
        }*/
        return R.ok();
    }

    public R upload2(MultipartFile file) {
        Map<String, List<List<String>>> excelDataMap = ImportExcelUtils.readExcel(file, 1, 0, 0);
        List<List<String>> dataList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(excelDataMap)) {
            for (String key : excelDataMap.keySet()) {
                dataList = excelDataMap.get(key);
            }
        }
        List<ScanHostEntity> saveHostList = new ArrayList<>();
        List<ScanHostEntity> updateHostList = new ArrayList<>();
        List<ScanPortEntity> savePortList = new ArrayList<>();
        List<String> domainList = new ArrayList<>();
        List<Long> ipList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dataList)) {
            for (List<String> data : dataList) {
                if (StringUtils.isEmpty(data.get(3)) || StringUtils.isEmpty(data.get(4))) {
                    continue;
                }
                domainList.add(data.get(2));
                ipList.add(IpLongUtils.ipToLong(data.get(3)));
            }
        }

        Map<String, String> parentDomainMap = new HashMap<>();
        List<ScanHostEntity> existDomainList = scanHostFeign.getByDomainList(domainList);
        if (!CollectionUtils.isEmpty(existDomainList)) {
            for (ScanHostEntity domain : existDomainList) {
                parentDomainMap.put(domain.getDomain(), domain.getParentDomain());
            }
        }

        List<ScanHostEntity> existIpList = scanHostFeign.getByIpList(ipList);
        Map<String, List<ScanHostEntity>> ipMap = existIpList.stream().collect(Collectors.groupingBy(h->h.getIp()));

        List<ScanPortEntity> existPortList = scanPortFeign.getByIpList(ipList);
        List<String> ipPortList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(existPortList)) {
            for (ScanPortEntity port : existPortList) {
                ipPortList.add(port.getIp() + Const.STR_CROSSBAR + port.getPort());
            }
        }

        if (!CollectionUtils.isEmpty(dataList)) {
            for (List<String> data : dataList) {
                // scan_port表 ip不存在  或者 ip存在但端口不存在，存一条端口数据
                List<String> portList = PortUtils.getPortList(data.get(4));
                if (CollectionUtils.isEmpty(portList)) {
                    return R.error("端口不能为空！");
                }

                if (!ipMap.containsKey(data.get(3))) {
                    // scan_host表 不包含ip,新增
                    String domain = StringUtils.isEmpty(data.get(2)) ? data.get(3) : data.get(2);
                    // 子域名相同，ip不同，视为错误数据，不保存
                    if (parentDomainMap.containsKey(domain)) {
                        continue;
                    }
                    String parentDomain = Const.STR_EMPTY;
                    if (parentDomainMap.containsKey(domain)) {
                        parentDomain = parentDomainMap.get(domain);
                    } else {
                        parentDomain = StringUtils.isEmpty(data.get(1)) ? StringUtils.isEmpty(data.get(2)) ? data.get(3) : data.get(2) : data.get(1);
                    }
                    Integer isDomain = RexpUtil.isIP(domain) ? Const.INTEGER_0 : Const.INTEGER_1;
                    Integer isMajor = RexpUtil.isMajorDomain(domain) ? Const.INTEGER_1 : Const.INTEGER_0;
                    ScanHostEntity host = ScanHostEntity.builder()
                            .domain(domain)
                            .parentDomain(parentDomain)
                            .ip(data.get(3)).type(data.get(2).equals(data.get(1)) ? Const.INTEGER_1 : Const.INTEGER_3)
                            .scanPorts(data.get(4)).company(data.get(0))
                            .isDomain(isDomain).isMajor(isMajor)
                            .build();
                    saveHostList.add(host);
                } else {
                    List<ScanHostEntity> hostList = ipMap.get(data.get(3));
                    Map<String, ScanHostEntity> hosts = hostList.stream().collect(Collectors.toMap(ScanHostEntity::getDomain, s->s));
                    if (hosts.containsKey(data.get(2))) {
                        // scan_host表 包含ip,同时子域名相同，视为一条存在的数据（不管主域名）
                        ScanHostEntity host = hosts.get(data.get(2));
                        if (!PortUtils.portEquals(host.getScanPorts(), data.get(4))) {
                            host.setScanPorts(PortUtils.getAllPorts(host.getScanPorts(), data.get(4)));
                            updateHostList.add(host);
                        }
                    } else {
                        // scan_host表 包含ip,但子域名不同，视为新数据
                        ScanHostEntity host = ScanHostEntity.builder()
                                .domain(data.get(2)).parentDomain(data.get(1)).ip(data.get(3))
                                .type(Const.INTEGER_3).scanPorts(data.get(4)).company(data.get(0))
                                .build();
                        saveHostList.add(host);
                    }
                }

                for (String p : portList) {
                    if (!ipPortList.contains(data.get(3) + Const.STR_CROSSBAR + p)) {
                        ScanPortEntity port = ScanPortEntity.builder()
                                .ip(data.get(3)).port(Integer.valueOf(p))
                                .serverName(data.get(5))
                                .build();
                        savePortList.add(port);
                        ipPortList.add(data.get(3) + Const.STR_CROSSBAR + p);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(saveHostList)) {
            scanHostFeign.saveBatch(saveHostList);
        }
        if (!CollectionUtils.isEmpty(updateHostList)) {
            for (ScanHostEntity host : updateHostList) {
                scanHostFeign.update(host);
            }
            scanHostFeign.saveBatch(saveHostList);
        }
        if (!CollectionUtils.isEmpty(savePortList)) {
            scanPortFeign.saveBatch(savePortList);
        }
        return R.ok();
    }

    public R uploadCms(MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        File file = new File("./"+ fileName);
        try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(),file);
            String jsonString = FileUtils.readFileToString(file, "UTF-8");
            Gson gson = new Gson();
            FingerprintListDTO fingerprintListDTO = gson.fromJson(jsonString, FingerprintListDTO.class);
            List<CmsJsonDto> cmsJsonDtoList = fingerprintListDTO.getFingerprint();
            if (CollectionUtils.isEmpty(cmsJsonDtoList)) {
                return R.error("请上传正确的规则！");
            }
            List<CmsJsonEntity> newList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(cmsJsonDtoList)) {
                for (CmsJsonDto dto : cmsJsonDtoList) {
                    CmsJsonEntity entity = CmsJsonEntity.builder()
                            .cms(dto.getCms()).method(dto.getMethod())
                            .location(dto.getLocation())
                            .keywordList(dto.getKeyword())
                            .build();
                    if (!CollectionUtils.isEmpty(dto.getKeyword())) {
                        entity.setKeyword(String.join(Const.STR_COMMA, dto.getKeyword()));
                    }
                    newList.add(entity);
                }
            }
            List<CmsJsonEntity> oldList = cmsJsonFeign.list(new HashMap<>());
            // 保存新的规则
            newList.removeAll(oldList);
            if (!CollectionUtils.isEmpty(newList)) {
                cmsJsonFeign.saveBatch(newList);
            }
            // 更新规则缓存
            newList.addAll(oldList);
//            stringRedisTemplate.opsForValue().set(CacheConst.REDIS_CMS_JSON, JSON.toJSONString(newList));
//            List<CmsJsonEntity> domList = newList.stream().filter(f -> "keyword".equals(f.getMethod())).collect(Collectors.toList());
//            stringRedisTemplate.opsForValue().set(CacheConst.REDIS_CMS_JSON_LIST, JSON.toJSONString(domList));
//            Map<String, String> faviconMap = newList.stream().filter(f -> "faviconhash".equals(f.getMethod())).collect(Collectors.toMap(
//                    CmsJsonEntity::getKeyword, CmsJsonEntity::getCms, (key1, key2) -> key1));
//            stringRedisTemplate.opsForValue().set(CacheConst.REDIS_CMS_JSON_MAP, JSON.toJSONString(faviconMap));
        } catch (IOException e) {
            e.printStackTrace();
        }
        file.delete();
        return R.ok();
    }

    public void exportPorts(Map<String, Object> params, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            String fileName =  "repeatedWrite" + System.currentTimeMillis() + ".xlsx";
            excelWriter = EasyExcel.write(out, ScanPortVo.class).build();

            //  查询总数并封装相关变量(这块直接拷贝就行了不要改)
            Integer totalRowCount = scanPortFeign.exportNum(params);
            Integer perSheetRowCount = ExcelConstant.PER_SHEET_ROW_COUNT;
            Integer pageSize = ExcelConstant.PER_WRITE_ROW_COUNT;
            Integer sheetCount = totalRowCount % perSheetRowCount == 0 ? (totalRowCount / perSheetRowCount) : (totalRowCount / perSheetRowCount + 1);
            Integer previousSheetWriteCount = perSheetRowCount / pageSize;
            Integer lastSheetWriteCount = totalRowCount % perSheetRowCount == 0 ?
                    previousSheetWriteCount :
                    (totalRowCount % perSheetRowCount % pageSize == 0 ? totalRowCount % perSheetRowCount / pageSize : (totalRowCount % perSheetRowCount / pageSize + 1));

            for (int i = 0; i < sheetCount; i++) {
                //  创建SHEET
                WriteSheet writeSheet = EasyExcel.writerSheet("sheet"+i).build();

                //  写数据  这个j的最大值判断直接拷贝就行了，不要改动
                for (int j = 0; j < (i != sheetCount - 1 ? previousSheetWriteCount : lastSheetWriteCount); j++) {
                    params.put("page", j + 1 + previousSheetWriteCount * i);
                    params.put("limit", pageSize);
                    List<ScanPortVo> portList = scanPortFeign.exportList(params);
                    excelWriter.write(portList, writeSheet);
                }
            }
            //  下载EXCEL
            response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1"));
            response.setContentType("multipart/form-data");
            response.setCharacterEncoding("utf-8");
            excelWriter.finish();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void exportHoles(Map<String, Object> params, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            String fileName =  "repeatedWrite" + System.currentTimeMillis() + ".xlsx";
            excelWriter = EasyExcel.write(out, ScanHoleVo.class).build();

            //  查询总数并封装相关变量(这块直接拷贝就行了不要改)
            Integer totalRowCount = scanSecurityHoleFeign.exportNum(params);
            Integer perSheetRowCount = ExcelConstant.PER_SHEET_ROW_COUNT;
            Integer pageSize = ExcelConstant.PER_WRITE_ROW_COUNT;
            Integer sheetCount = totalRowCount % perSheetRowCount == 0 ? (totalRowCount / perSheetRowCount) : (totalRowCount / perSheetRowCount + 1);
            Integer previousSheetWriteCount = perSheetRowCount / pageSize;
            Integer lastSheetWriteCount = totalRowCount % perSheetRowCount == 0 ?
                    previousSheetWriteCount :
                    (totalRowCount % perSheetRowCount % pageSize == 0 ? totalRowCount % perSheetRowCount / pageSize : (totalRowCount % perSheetRowCount / pageSize + 1));

            for (int i = 0; i < sheetCount; i++) {
                //  创建SHEET
                WriteSheet writeSheet = EasyExcel.writerSheet("sheet"+i).build();

                //  写数据  这个j的最大值判断直接拷贝就行了，不要改动
                for (int j = 0; j < (i != sheetCount - 1 ? previousSheetWriteCount : lastSheetWriteCount); j++) {
                    params.put("page", j + 1 + previousSheetWriteCount * i);
                    params.put("limit", pageSize);
                    List<ScanHoleVo> portList = scanSecurityHoleFeign.exportList(params);
                    excelWriter.write(portList, writeSheet);
                }
            }
            //  下载EXCEL
            response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1"));
            response.setContentType("multipart/form-data");
            response.setCharacterEncoding("utf-8");
            excelWriter.finish();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void uploadPorts(Map<String, Object> params) {
        params.put("exportType", ExportTypeEnum.port.getType());
        filePushSender.putExport(params);
    }

    public void uploadHoles(Map<String, Object> params) {
        params.put("exportType", ExportTypeEnum.hole.getType());
        filePushSender.putExport(params);
    }

    public R exportFiles(List<String> fileUrlList) {
        List<String> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(fileUrlList)) {
            for (String fileUrl : fileUrlList) {
                String url = fileService.download(defBucket, fileUrl);
                list.add(url);
            }
        }
        return R.ok().put("data", list);
    }

    public R uploadFiles(List<MultipartFile> files, Integer toolType){
        List<HoleYamlEntity> saveList = new ArrayList<>();
        for (MultipartFile file : files) {
            FileInfoDTO dto = null;
            String[] arr = file.getOriginalFilename().split(Const.STR_SLASH);
            String fileName = arr[arr.length-1];
            try {
                dto = fileService.uploadFile(defBucket, file.getInputStream(), fileName, "hole");
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

}
