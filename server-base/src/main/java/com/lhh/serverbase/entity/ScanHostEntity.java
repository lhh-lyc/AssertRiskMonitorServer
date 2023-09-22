package com.lhh.serverbase.entity;

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
@TableName("scan_host")
public class ScanHostEntity extends BaseEntity {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long hostId;
    /**
     * 父级域名
     */
    private String parentDomain;
    /**
     * 域名
     */
    private String domain;
    /**
     * ip
     */
    @TableField(exist = false)
    private String ip;
    /**
     * ip
     */
    private Long ipLong;
    /**
     * 扫描的端口
     */
    private String scanPorts;
    /**
     * 域名所属单位
     */
    private String company;
    /**
     * host类型 1.录入域名 2.录入ip 3.子域名
     */
    private Integer type;
    /**
     * 是否为域名（0.否 1.是）
     */
    private Integer isDomain;
    /**
     * 是否为主域名（0.否 1.是）
     */
    private Integer isMajor;
    /**
     * 是否正在扫描中（0.否 1.是）防止重复扫描
     */
    private Integer isScanning;
    /**
     * 项目名称
     */
    @TableField(exist = false)
    private String projectName;
    /**
     * 端口
     */
    @TableField(exist = false)
    private String port;
    /**
     * 服务
     */
    @TableField(exist = false)
    private String serverName;
    @TableField(exist = false)
    private Long userId;
    @TableField(exist = false)
    private String userName;
    /**
     * ip
     */
    @TableField(exist = false)
    private String url;
    /**
     * ip
     */
    @TableField(exist = false)
    private String title;
    /**
     * ip
     */
    @TableField(exist = false)
    private String cms;

    @TableField(exist = false)
    private List<String> subIpList;

    @TableField(exist = false)
    private List<Integer> scanPortList;

}
