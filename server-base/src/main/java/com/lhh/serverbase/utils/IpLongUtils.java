package com.lhh.serverbase.utils;

import com.lhh.serverbase.common.constant.Const;
import org.apache.commons.net.util.SubnetUtils;

public class IpLongUtils {

    /**
     * 把字符串IP转换成long
     *
     * @param ipStr 字符串IP
     * @return IP对应的long值
     */
    public static long ipToLong(String ipStr) {
        if (Const.STR_CROSSBAR.equals(ipStr)) {
            return Const.LONG_0;
        }
        String[] ip = ipStr.split("\\.");
        if (ip != null && ip.length < 4) {
            return Const.LONG_0;
        }
        return (Long.valueOf(ip[0]) << 24) + (Long.valueOf(ip[1]) << 16)
                + (Long.valueOf(ip[2]) << 8) + Long.valueOf(ip[3]);
    }

    /**
     * 把IP的long值转换成字符串
     *
     * @param ipLong IP的long值
     * @return long值对应的字符串
     */
    public static String longToIp(long ipLong) {
        if (Const.LONG_0.equals(ipLong)) {
            return Const.STR_CROSSBAR;
        }
        StringBuilder ip = new StringBuilder();
        ip.append(ipLong >>> 24).append(".");
        ip.append((ipLong >>> 16) & 0xFF).append(".");
        ip.append((ipLong >>> 8) & 0xFF).append(".");
        ip.append(ipLong & 0xFF);
        return ip.toString();
    }

    public static void main(String[] args) {
        System.out.println("ip转整数型：" + ipToLong("85.208.116.239"));
        System.out.println("整数型转ip：" + longToIp(0));

        SubnetUtils utils = new SubnetUtils("192.168.1.0/12");
        String[] allIps = utils.getInfo().getAllAddresses();
        System.out.println(allIps);
    }

}
