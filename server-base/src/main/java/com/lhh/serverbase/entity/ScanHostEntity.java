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
    private String ip;
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

    @TableField(exist = false)
    private List<String> subIpList;

    @TableField(exist = false)
    private List<Integer> scanPortList;

}
