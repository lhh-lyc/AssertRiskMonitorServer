package com.lhh.servermonitor.dto;

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

    private String parentDomain;

    private String subDomain;

    private String subIp;

    private String ports;

    private List<String> subIpList;

}