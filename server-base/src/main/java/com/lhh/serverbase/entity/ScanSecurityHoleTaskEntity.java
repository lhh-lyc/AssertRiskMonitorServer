package com.lhh.serverbase.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

/**
 * 系统_用户角色表
 *
 * @author lyc
 * @date 2023-12-28 17:49:19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("scan_security_hole_task")
public class ScanSecurityHoleTaskEntity extends BaseEntity {

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
     * 主域名
     */
    private String domain;
    /**
     * 子域名
     */
    private String subDomain;
    /**
     * 扫描的端口
     */
    private String scanPorts;
    /**
     * 1.正常扫描 1.重新扫描
     */
    private Integer type;
}
