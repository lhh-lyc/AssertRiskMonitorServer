package com.lhh.serverbase.common.utils;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ThreadUtil {



    /**
     * 将列表按列表总长度划分
     *
     * @param originList 数据
     * @param num 份数
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> divideByCopies(List<T> originList, int num) {
        List<List<T>> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(originList) || num < 0) {
            return null;
        }
        for (int i = 0; i < num; i++) {
            list.add(new ArrayList<T>());
        }
        for (int i = 0; i < originList.size(); i++) {
            list.get(i % num).add(originList.get(i));
        }

        return list;
    }

}
