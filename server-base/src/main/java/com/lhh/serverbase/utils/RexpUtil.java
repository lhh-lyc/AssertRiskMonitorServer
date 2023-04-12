package com.lhh.serverbase.utils;

import com.lhh.serverbase.common.constant.RexpConst;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RexpUtil {

    /**
     * 判断IP格式和范围
     */
    public static boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }
        Pattern pat = Pattern.compile(RexpConst.ipRex);
        Matcher mat = pat.matcher(addr);
        boolean ipAddress = mat.find();
        return ipAddress;
    }

    /**
     * 验证是否为一级域名
     * @param domain
     * @return
     */
    public static boolean isDomain(String domain) {
        if (domain.matches(RexpConst.domainRex)) {
            return true;
        }
        return false;
    }

    /**
     * 验证二级域名是否合法
     * @param domain
     * @return
     */
    public static boolean isSubDomain(String domain) {
        if (domain.matches(RexpConst.subdomainRex)) {
            return true;
        }
        return false;
    }

}
