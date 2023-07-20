package com.lhh.serverbase.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-07-12 15:38:11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("scan_host_port")
public class ScanHostPortEntity extends BaseEntity {

    /**
     * 项目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 域名
     */
    private String domain;
    /**
     * port
     */
    private Integer port;
    /**
     *
     */
    private String url;
    /**
     *
     */
    private String title;
    /**
     *
     */
    private String cms;
}
