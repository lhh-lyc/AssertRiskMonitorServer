package com.lhh.serverbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统_菜单表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_menu")
public class SysMenuEntity extends BaseEntity {

    /**
     * 菜单ID
     */
    @TableId(type = IdType.AUTO)
    private Long menuId;
    /**
     * 父菜单ID，一级菜单为0
     */
    private Long parentId;
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 菜单URL
     */
    private String url;
    /**
     * 授权(多个用逗号分隔，如：user:list,user:create)
     */
    private String perms;
    /**
     * 排序
     */
    private Integer orderNum;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 类型   0：目录   1：菜单   2：按钮
     */
    private Integer type;
    /**
     * 备注
     */
    private String remark;
}
