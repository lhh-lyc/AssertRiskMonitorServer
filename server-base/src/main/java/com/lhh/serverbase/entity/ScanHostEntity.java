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
     * 域名或ip
     */
    private String host;
    /**
     * 父级域名
     */
    private String parentHost;
    /**
     * 扫描的端口
     */
    private String scanPorts;
    /**
     * host类型 1.录入域名 2.录入ip 3.子域名
     */
    private Integer type;

    @TableField(exist = false)
    private String ports;

    @TableField(exist = false)
    private List<String> subIpList;

    @TableField(exist = false)
    private List<Integer> scanPortList;

}
