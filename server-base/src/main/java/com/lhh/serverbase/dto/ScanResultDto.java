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
public class ScanResultDto {

    private Long projectId;

    private String company;

    private String domain;

    private String parentDomain;

    private String ip;

    private Integer port;

    private Integer isDomain;

    private Integer isMajor;

}
