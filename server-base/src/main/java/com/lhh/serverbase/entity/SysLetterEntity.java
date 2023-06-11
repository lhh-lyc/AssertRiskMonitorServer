package com.lhh.serverbase.entity;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-06-11 12:18:45
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_letter")
public class SysLetterEntity extends BaseEntity {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 域名
     */
    private String content;
    /**
     * 端口
     */
    private String scanPorts;
    /**
     * 状态  0：未读 1：已读
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String statusName;

    @TableField(exist = false)
    private List<String> domainList;
}
