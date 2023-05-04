package com.lhh.serverbase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanParamDto implements Serializable {

    private Long projectId;

    private Long hostId;

    private String host;

    private String domain;

    private String subDomain;

    private String subIp;

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

    List<ScanParamDto> dtoList;

}
