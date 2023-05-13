package com.lhh.serverbase.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lhh.serverbase.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author lyc
 * @date 2023-02-23 19:21:07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanPortVo {
    /**
     * 公司
     */
    @ExcelProperty("用户")
    @TableField(exist = false)
    private String userName;
    /**
     * 公司
     */
    @ColumnWidth(20)
    @ExcelProperty("项目")
    @TableField(exist = false)
    private String projectName;
    /**
     * 公司
     */
    @ColumnWidth(30)
    @ExcelProperty("公司")
    @TableField(exist = false)
    private String company;
    /**
     * 主域名
     */
    @ColumnWidth(20)
    @ExcelProperty("主域名")
    @TableField(exist = false)
    private String parentDomain;
    /**
     * 子域名
     */
    @ColumnWidth(30)
    @ExcelProperty("子域名")
    @TableField(exist = false)
    private String domain;
    /**
     * ip
     */
    @ColumnWidth(20)
    @ExcelProperty("ip")
    private String ip;
    /**
     * port
     */
    @ExcelProperty("端口")
    private Integer port;
    /**
     * 端口对应的server
     */
    @ColumnWidth(20)
    @ExcelProperty("服务")
    private String serverName;

}
