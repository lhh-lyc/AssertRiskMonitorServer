package com.lhh.serverbase.enums;

import com.lhh.serverbase.common.constant.Const;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExportTypeEnum {

    port(1, "port"),

    hole(2, "hole");

    private final Integer type;

    private final String info;

}
