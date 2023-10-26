package com.lhh.serverbase.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
@TableName("host_company")
public class HostCompanyEntity extends BaseEntity{

    /**
     * 项目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 主域名
     */
    private String host;
    /**
     * 公司
     */
    private String company;
    /**
     * 扫描的端口
     */
    private String scanPorts;
    /**
     * 扫描完成时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date scanTime;

}
