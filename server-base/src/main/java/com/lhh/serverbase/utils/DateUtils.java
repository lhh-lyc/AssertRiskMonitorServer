package com.lhh.serverbase.utils;

import cn.hutool.core.date.DateUtil;
import com.lhh.serverbase.common.constant.Const;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
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

    /**
     * 返回"yyyy-MM-dd HH:mm:ss"格式时间
     *
     * @return 加/减几天后的日期
     */
    public static String getYMDHms(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 对时间的【小时】进行加/减
     *
     * @param date    日期
     * @param hours 小时数，负数为减
     * @return 加/减几小时后的时间
     */
    public static Date addDateHours(Date date, int hours) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusHours(hours).toDate();
    }

    public static boolean isInTwoWeek(Date addTime,Date now, Integer days) {
        if (addTime == null || days == null) {
            return false;
        }
        if (Const.INTEGER_0.equals(days)) {
            return true;
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
