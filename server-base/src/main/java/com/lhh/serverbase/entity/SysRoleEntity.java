package com.lhh.serverbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统_角色表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_role")
public class SysRoleEntity extends BaseEntity {

    /**
     * 角色ID
     */
    @TableId(type = IdType.AUTO)
    private Long roleId;
    /**
     * 单位id
     */
    private Long unitId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 备注
     */
    private String remark;
}
