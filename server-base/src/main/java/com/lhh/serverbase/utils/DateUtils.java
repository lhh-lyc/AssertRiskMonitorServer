package com.lhh.serverbase.utils;

import cn.hutool.core.date.DateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Value;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 对象复制
 *
 * @author Rona
 * @date 2018/12/21 11:54
 */
public class DateUtils {

    public static boolean isInTwoWeek(Date addTime,Date now, Integer days) {
        if (addTime == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        Date before7days = calendar.getTime();
        if(before7days.getTime() < addTime.getTime()){
            return true;
        }else{
            return false;
        }
    }

}
