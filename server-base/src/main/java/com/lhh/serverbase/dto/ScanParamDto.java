package com.lhh.serverbase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanParamDto {

    private Long projectId;

    private Long hostId;

    private String host;

    private String domain;

    private String subDomain;

    private String subIp;

    private String scanPorts;

    private List<String> subIpList;

}
