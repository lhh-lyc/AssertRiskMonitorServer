package com.lhh.servermonitor.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperateEnum {

    scanSubdomain(1, "获取子域名"),

    subdomainToIp(2, "子域名转ip"),

    scanPort(3, "扫描开放端口");

    private final Integer type;

    private final String desc;

}
