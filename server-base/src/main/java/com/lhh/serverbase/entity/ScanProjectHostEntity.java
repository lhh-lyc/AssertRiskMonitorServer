package com.lhh.serverbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统_用户角色表
 *
 * @author lyc
 * @date 2023-03-06 19:24:41
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("scan_project_host")
public class ScanProjectHostEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 项目ID
     */
    private Long projectId;
    /**
     * host表
     */
    private String host;
    /**
     * 是否正在扫描中（0.否 1.是）防止重复扫描
     */
    private Integer isScanning;

    @TableField(exist = false)
    private String ports;

}
