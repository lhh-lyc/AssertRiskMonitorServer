package com.lhh.serverbase.enums;

import cn.hutool.core.util.ReUtil;
import com.lhh.serverbase.common.constant.Const;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LevelEnum {

    medium(1, "medium"),

    high(2, "high"),

    critical(3, "critical");

    private final Integer level;

    private final String info;

    public static Integer getLevel(String info){
        LevelEnum[] levelEnums = LevelEnum.values();
        for (LevelEnum item : levelEnums) {
            if (item.getInfo().equals(info.toLowerCase())) {
                return item.getLevel();
            }
        }
        return Const.INTEGER_0;
    }

}
