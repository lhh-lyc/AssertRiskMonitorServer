package com.lhh.serverbase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 系统_文件表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_files")
public class SysFilesEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 关联表(ent:企业,mon:监测点,)
     */
    private String uuid;
    /**
     * 创建人ID
     */
    private Long createId;
    /**
     * 创建人
     */
    private String createName;
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
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * 删除标识（0 正常 1 删除）
     */
    private Integer delFlg;
}
