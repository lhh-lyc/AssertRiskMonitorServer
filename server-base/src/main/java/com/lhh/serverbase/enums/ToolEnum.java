package com.lhh.serverbase.enums;

import com.lhh.serverbase.common.constant.Const;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ToolEnum {

    nuclei(1, "nuclei"),

    afrog(2, "afrog"),

    xray(3, "xray");

    private final Integer toolType;

    private final String tool;

    public static Integer getTool(String tool){
        ToolEnum[] toolEnums = ToolEnum.values();
        for (ToolEnum item : toolEnums) {
            if (item.getTool().equals(tool)) {
                return item.getToolType();
            }
        }
        return Const.INTEGER_0;
    }

}
