package com.lhh.serverbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 系统_日志表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_log")
public class SysLogEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户种类(0:系统用户,1:助理用户,2:客户)
     */
    private Integer userType;
    /**
     * 平台类型(0:默认PC,1:APP,2:公众号)
     */
    private Integer platType;
    /**
     * 操作
     */
    private String operation;
    /**
     * 方法
     */
    private String method;
    /**
     * 参数
     */
    private String params;
    /**
     * 执行时间
     */
    private Long execTime;
    /**
     * IP地址
     */
    private String ip;
    /**
     * 创建时间
     */
    private Date createTime;
}
