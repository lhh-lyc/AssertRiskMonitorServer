package com.lhh.serverbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("hole_yaml")
public class HoleYamlEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     *
     */
    private Long userId;
    /**
     * 创建人ID
     */
    private Long createId;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件桶名称
     */
    private String bucketName;
    /**
     * 文件类型(img,xls,doc,mp4)
     */
    private String fileType;
    /**
     * toolType
     */
    private Integer toolType;
    /**
     * 文件大小(KB)
     */
    private Double fileSize;
    /**
     * 文件url
     */
    private String fileUrl;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建人
     */
    @TableField(exist = false)
    private String userName;
}
