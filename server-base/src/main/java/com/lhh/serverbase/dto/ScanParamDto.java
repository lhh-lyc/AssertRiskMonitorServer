package com.lhh.serverbase.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanParamDto implements Serializable {

    private static final long serialVersionUID = 1;

    private Long projectId;

    private Long hostId;

    private String host;

    private String domain;

    private String subDomain;

    private String subIp;

    private String allPorts;

    private String scanPorts;

    private List<String> subIpList;
    /**
     * 是否收集子域名（0.否 1.是）
     */
    private Integer subDomainFlag;
    /**
     * 是否扫描端口（0.否 1.是）
     */
    private Integer portFlag;
    /**
     * 扫描端口工具（1.masscan 2.nmap）
     */
    private Integer portTool;
    /**
     * 扫描完成时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date scanTime;

    List<ScanParamDto> dtoList;

}
