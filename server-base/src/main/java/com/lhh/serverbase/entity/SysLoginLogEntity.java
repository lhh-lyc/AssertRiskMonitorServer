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
 * 系统_登录日志表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_login_log")
public class SysLoginLogEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 类型(0:登录,1:登出)
     */
    private Integer type;
    /**
     * 平台类型(0:默认PC,1:APP,2:公众号)
     */
    private String platType;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * IP地址
     */
    private String ip;
    /**
     * 创建时间
     */
    private Date createTime;
}
