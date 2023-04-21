package com.lhh.serverbase.utils;

import java.util.Map;
import java.util.TreeMap;

public class SortUtils {

    /**
     * map按key排序
     *
     * @param map
     * @return
     */
    public static Map<String, String> sortMapInteger(Map<String, String> map) {
        Map<String, String> resultMap = new TreeMap<>((str1, str2) -> Integer.valueOf(str1).compareTo(Integer.valueOf(str2)));
        resultMap.putAll(map);
        return resultMap;
    }

}
