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
 * 系统_用户角色表
 *
 * @author lyc
 * @date 2023-03-06 19:24:41
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("scan_project_content")
public class ScanProjectContentEntity extends BaseEntity {

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
     * 输入域名或ip
     */
    private String inputHost;
    /**
     * 扫描的端口
     */
    private String scanPorts;
    /**
     * 是否完成
     */
    private Integer isCompleted;

    @TableField(exist = false)
    private String ports;

    @TableField(exist = false)
    private List<String> ipList;
}
