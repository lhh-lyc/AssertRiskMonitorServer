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
 * @date 2023-03-16 17:05:59
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("scan_host_ip")
public class ScanHostIpEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * host
     */
    private String host;
    /**
     * ip
     */
    private String ip;
}
