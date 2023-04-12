package com.lhh.serverbase.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-02-23 19:21:08
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("scan_add_record")
public class ScanAddRecordEntity extends BaseEntity {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;
    /**
     * 新增记录类型
     */
    private Integer addRecordType;
    /**
     * 对应的父类对象
     */
    private String parentName;
    /**
     * 对应的子类对象
     */
    private String subName;

    @TableField(exist = false)
    private String projectName;

    @TableField(exist = false)
    private String describe;
}
