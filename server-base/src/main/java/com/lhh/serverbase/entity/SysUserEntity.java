package com.lhh.serverbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_user")
public class SysUserEntity extends BaseEntity {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户名
     */
    private String realName;
    /**
     * 密码
     */
    private String password;
    /**
     * 盐
     */
    private String salt;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 状态  0：正常 1：禁用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private List<Long> roleIdList;
}
