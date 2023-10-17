package com.lhh.serverbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-09-12 15:41:27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("scan_security_hole")
public class ScanSecurityHoleEntity extends BaseEntity {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;
    /**
     * 域名
     */
    private String domain;
    /**
     * 子域名
     */
    private String subDomain;
    /**
     * 漏洞名称
     */
    private String name;
    /**
     * 漏洞级别 1.medium 2.high 3.critical
     */
    private Integer level;
    /**
     * 协议
     */
    private String protocol;
    /**
     * 请求路径
     */
    private String url;
    /**
     * 描述
     */
    private String info;
    /**
     * 漏洞状态 1.未修复 2.已修复 3.修复误报
     */
    private Integer status;
    /**
     * 扫描工具字典值
     */
    private Integer toolType;
    /**
     * url 参数之前的部分
     */
    private String preUrl;
    /**
     * 备注
     */
    private String remark;
    /**
     * 用户名称
     */
    @TableField(exist = false)
    private String userName;
    /**
     * 项目名称
     */
    @TableField(exist = false)
    private String projectName;
    /**
     * 状态名称
     */
    @TableField(exist = false)
    private String statusName;
    /**
     * 工具名称
     */
    @TableField(exist = false)
    private String toolTypeName;
    /**
     * 等级名称
     */
    @TableField(exist = false)
    private String levelName;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (StringUtils.isEmpty(name) ? 0 : name.hashCode());
        result = prime * result + (level == null ? 0 : level.hashCode());
        result = prime * result + (StringUtils.isEmpty(protocol)? 0 : protocol.hashCode());
        result = prime * result + (StringUtils.isEmpty(preUrl) ? 0 : preUrl.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ScanSecurityHoleEntity hole = (ScanSecurityHoleEntity) obj;
        if (StringUtils.isEmpty(name)) {
            if (!StringUtils.isEmpty(hole.name)) {
                return false;
            }
        } else if (!name.equals(hole.name)) {
            return false;
        }
        if (level == null) {
            if (hole.level != null) {
                return false;
            }
        } else if (!level.equals(hole.level)) {
            return false;
        }
        if (StringUtils.isEmpty(protocol)) {
            if (!StringUtils.isEmpty(hole.protocol)) {
                return false;
            }
        } else if (!protocol.equals(hole.protocol)) {
            return false;
        }
        if (StringUtils.isEmpty(preUrl)) {
            if (!StringUtils.isEmpty(hole.preUrl)) {
                return false;
            }
        } else if (!preUrl.equals(hole.preUrl)) {
            return false;
        }
        return true;
    }

}
