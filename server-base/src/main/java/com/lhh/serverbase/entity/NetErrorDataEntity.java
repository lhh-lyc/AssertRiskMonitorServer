package com.lhh.serverbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-06-25 15:49:24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("net_error_data")
public class NetErrorDataEntity {

    /**
     * 项目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 对象
     */
    private String obj;
    /**
     * 扫描的端口
     */
    private String scanPorts;
    /**
     * 类型(1.更新project_host 2.跟新host表带端口3.重扫更新host表)
     */
    private Integer type;
}
