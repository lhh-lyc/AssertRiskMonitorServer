package com.lhh.serverbase.entity;

import java.util.Date;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-02-23 19:21:07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("scan_port")
public class ScanPortEntity extends BaseEntity {

    /**
     * 项目ID
     */
    @TableId(type = IdType.AUTO)
    private Long portId;
    /**
     * ip
     */
    private String ip;
    /**
     * port
     */
    private Integer port;
    /**
     * 端口对应的server
     */
    private String serverName;
    /**
     * 子域名
     */
    @TableField(exist = false)
    private String domain;
    /**
     * 主域名
     */
    @TableField(exist = false)
    private String parentDomain;
    /**
     * 公司
     */
    @TableField(exist = false)
    private String company;

}
