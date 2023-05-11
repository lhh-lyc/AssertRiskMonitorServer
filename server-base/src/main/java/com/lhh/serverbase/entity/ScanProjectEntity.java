package com.lhh.serverbase.entity;

import java.io.Serializable;
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
 * @date 2023-02-23 19:21:08
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("scan_project")
public class ScanProjectEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * 项目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 是否收集子域名（0.否 1.是）
     */
    private Integer subDomainFlag;
    /**
     * 是否扫描端口（0.否 1.是）
     */
    private Integer portFlag;
    /**
     * 扫描的端口
     */
    private String scanPorts;
    /**
     * 是否为单次探测（0.否 1.是）
     */
    private Integer isSingleScan;
    /**
     * 探测开始时间
     */
    private Date scanBeginTime;
    /**
     * 探测结束时间
     */
    private Date scanEndTime;
    /**
     * 探测时间间隔
     */
    private String scanInterval;
    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private List<String> hostList;

    @TableField(exist = false)
    private String hosts;

    @TableField(exist = false)
    private String queueId;

    @TableField(exist = false)
    private Integer allHostNum;

    @TableField(exist = false)
    private Integer scannedHostNum;

    @TableField(exist = false)
    private Integer portNum;

    @TableField(exist = false)
    private Integer isCompleted;

    @TableField(exist = false)
    private String userName;

}
