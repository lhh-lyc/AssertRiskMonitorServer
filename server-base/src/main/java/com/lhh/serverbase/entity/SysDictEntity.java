package com.lhh.serverbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统_字典表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_dict")
public class SysDictEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 字典名称
     */
    private String name;
    /**
     * 字典类型
     */
    private String type;
    /**
     * 字典码
     */
    private String code;
    /**
     * 字典值
     */
    private String value;
    /**
     * 排序
     */
    private String orderNum;
    /**
     * 备注
     */
    private String remark;
}
