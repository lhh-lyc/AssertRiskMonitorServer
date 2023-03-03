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
    private Long id;
    /**
     * 项目ID
     */
    private Long projectId;
    /**
     * 主机ip表ID
     */
    private Long hostId;
    /**
     * port
     */
    private String port;
    /**
     * 端口对应的server
     */
    private String serverName;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 删除标识（0 正常 1 删除）
     */
    private Integer delFlg;
    /**
     *
     */
    private Long createId;
    /**
     *
     */
    private Long updateId;
}
