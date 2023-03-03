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
     * 项目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
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
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 删除标识（0 正常 1 删除）
     */
    private Integer delFlg;
    /**
     *
     */
    private Long createId;
    /**
     *
     */
    private Long updateId;
}
