package com.lhh.serverbase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传文件信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileInfoDTO {
    /**
     * 文件关联表主键ID
     */
    private Long fileId;
    /**
     * 文件桶名称
     */
    private String bucketName;
    /**
     * 文件名
     */
    private String fileOrgName;

    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件类型(img,xls,doc,mp4)
     */
    private String fileType;
    /**
     * 文件大小(KB)
     */
    private Double fileSize;
    /**
     * 文件url
     */
    private String fileUrl;
}
