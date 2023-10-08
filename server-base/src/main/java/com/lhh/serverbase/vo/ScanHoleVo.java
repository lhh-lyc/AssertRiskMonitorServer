package com.lhh.serverbase.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.TableField;
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
public class ScanHoleVo {

    /**
     * 用户
     */
    @ExcelProperty("用户")
    private String userName;

    @ColumnWidth(20)
    @ExcelProperty("项目")
    private String projectName;
    /**
     * 主域名
     */
    @ColumnWidth(20)
    @ExcelProperty("主域名")
    @TableField(exist = false)
    private String domain;
    /**
     * 子域名
     */
    @ColumnWidth(30)
    @ExcelProperty("子域名")
    @TableField(exist = false)
    private String subDomain;
    /**
     * 状态
     */
    @ExcelProperty("状态")
    private String statusName;
    /**
     * 扫描工具
     */
    @ColumnWidth(15)
    @ExcelProperty("扫描工具")
    private String toolTypeName;
    /**
     * 漏洞
     */
    @ColumnWidth(40)
    @ExcelProperty("漏洞")
    private String name;
    /**
     * 漏洞级别
     */
    @ColumnWidth(15)
    @ExcelProperty("漏洞级别")
    private String levelName;
    /**
     * 协议
     */
    @ExcelProperty("协议")
    private String protocol;
    /**
     * url
     */
    @ColumnWidth(60)
    @ExcelProperty("url")
    private String url;
    /**
     * info
     */
    @ColumnWidth(60)
    @ExcelProperty("info")
    private String info;

}
