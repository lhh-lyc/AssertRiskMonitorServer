package com.lhh.serverbase.utils;

import java.util.UUID;

/**
 * @author wuxu
 */
public class UuidUtils {
    /**
     * 获得uuid
     *
     * @return
     */
    public static String getUuid() {
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString();
        return uuidStr.replace("-", "");
//        return uuid.toString().replace("-", "");
    }
}
