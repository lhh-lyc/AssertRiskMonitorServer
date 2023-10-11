package com.lhh.serverexport.mqtt;

import cn.hutool.core.map.MapUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson2.JSON;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.constant.ExcelConstant;
import com.lhh.serverbase.dto.FileInfoDTO;
import com.lhh.serverbase.entity.SysFilesEntity;
import com.lhh.serverbase.enums.ExportTypeEnum;
import com.lhh.serverbase.utils.UuidUtils;
import com.lhh.serverbase.vo.ScanHoleVo;
import com.lhh.serverbase.vo.ScanPortVo;
import com.lhh.serverexport.feign.ScanPortFeign;
import com.lhh.serverexport.feign.ScanSecurityHoleFeign;
import com.lhh.serverexport.feign.SysFilesFeign;
import com.lhh.serverexport.service.FileService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(value = "exportData", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(name = "amp.topic"))})
public class ProjectListener {

    @Value("${my-config.upload.defFolder}")
    private String defFolder;
    @Value("${my-config.upload.defBucket}")
    private String defBucket;

    @Autowired
    ScanSecurityHoleFeign scanSecurityHoleFeign;
    @Autowired
    ScanPortFeign scanPortFeign;
    @Autowired
    SysFilesFeign sysFilesFeign;
    @Autowired
    FileService fileService;

    @RabbitHandler
    public void processMessage(byte[] bytes, Message message, Channel channel) {
        ByteArrayOutputStream out = null;
        try {
            Map<String, Object> params = (Map<String, Object>) JSON.parse((String) SerializationUtils.deserialize(bytes));
            out = new ByteArrayOutputStream();
            String filename = MapUtil.getStr(params, "filename");
            ExcelWriter excelWriter = null;

            Integer totalRowCount = Const.INTEGER_0;
            Integer type = MapUtil.getInt(params, "exportType");
            if (ExportTypeEnum.port.getType().equals(type)) {
                excelWriter = EasyExcel.write(out, ScanPortVo.class).build();
                totalRowCount = scanPortFeign.exportNum(params);
            }
            if (ExportTypeEnum.hole.getType().equals(type)) {
                excelWriter = EasyExcel.write(out, ScanHoleVo.class).build();
                totalRowCount = scanSecurityHoleFeign.exportNum(params);
            }
            Integer perSheetRowCount = ExcelConstant.PER_SHEET_ROW_COUNT;
            Integer pageSize = ExcelConstant.PER_WRITE_ROW_COUNT;
            Integer sheetCount = totalRowCount % perSheetRowCount == 0 ? (totalRowCount / perSheetRowCount) : (totalRowCount / perSheetRowCount + 1);
            Integer previousSheetWriteCount = perSheetRowCount / pageSize;
            Integer lastSheetWriteCount = totalRowCount % perSheetRowCount == 0 ?
                    previousSheetWriteCount :
                    (totalRowCount % perSheetRowCount % pageSize == 0 ? totalRowCount % perSheetRowCount / pageSize : (totalRowCount % perSheetRowCount / pageSize + 1));

            for (int i = 0; i < sheetCount; i++) {
                WriteSheet writeSheet = EasyExcel.writerSheet("sheet" + i).build();
                for (int j = 0; j < (i != sheetCount - 1 ? previousSheetWriteCount : lastSheetWriteCount); j++) {
                    params.put("page", j + 1 + previousSheetWriteCount * i);
                    params.put("limit", pageSize);
                    if (ExportTypeEnum.port.getType().equals(type)) {
                        List<ScanPortVo> portList = scanPortFeign.exportList(params);
                        excelWriter.write(portList, writeSheet);
                    }
                    if (ExportTypeEnum.hole.getType().equals(type)) {
                        List<ScanHoleVo> portList = scanSecurityHoleFeign.exportList(params);
                        excelWriter.write(portList, writeSheet);
                    }
                }
            }
            excelWriter.finish();
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String time = sdf.format(now);
            if (filename.contains("(")) {
                filename = filename.replace("(", time + "(");
            } else {
                filename += time;
            }
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
            FileInfoDTO file = fileService.uploadFile(defBucket, byteArrayInputStream, filename + ".xlsx", defFolder);
            FileInfoDTO dto = fileService.fileDetail(defBucket, file.getFileName());
            SysFilesEntity saveFile = SysFilesEntity.builder()
                    .fileName(filename).fileType(".xlsx")
                    .fileSize(dto.getFileSize()).fileUrl(file.getFileUrl())
                    .createTime(now).updateTime(now)
                    .createId(MapUtil.getLong(params, "userId") == null ? Const.LONG_1 : MapUtil.getLong(params, "userId"))
                    .uuid(UuidUtils.getUuid()).delFlg(Const.INTEGER_0).type(type)
                    .build();
            sysFilesFeign.save(saveFile);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
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

}
