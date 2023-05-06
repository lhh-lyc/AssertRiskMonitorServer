package com.lhh.serverbase.utils;

import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.constant.RexpConst;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
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
     * 1.顶级域名不解析
     * 2.未收录域名不解析
     * 3.
     */


    /**
     * 验证二级域名是否合法
     *
     * @param domain
     * @return
     */
    public static boolean isSubDomain(String domain) {
        if (domain.matches(RexpConst.subdomainRex)) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        String a = "    aaaaaa      \n bbbbbb  ";
        System.out.println(a.replace(" ", ""));
//        String domainName = "";
//        String b = "";
//        Boolean flag;
//        flag = isMajorDomain("xinghuodao.cn");
//        System.out.println(flag);
//        domainName = "baidu.com";
//        b = getMajorDomain(domainName);
//        System.out.println(b);
//        domainName = "ca.baidu.com";
//        b = getMajorDomain(domainName);
//        System.out.println(b);
//        domainName = "cat.ac.cn";
//        b = getMajorDomain(domainName);
//        System.out.println(b);
//        domainName = "ac.cn";
//        b = getMajorDomain(domainName);
//        System.out.println(b);
//        domainName = "xinghuodao.cn";
//        b = getMajorDomain(domainName);
//        System.out.println(b);
//        domainName = "cn";
//        b = getMajorDomain(domainName);
//        System.out.println(b);
//        domainName = "cc.baidu";
//        b = getMajorDomain(domainName);
//        System.out.println(b);
    }

    /**
     * 验证是否为一级域名
     *
     * @param url
     * @return
     */
    public static Boolean isMajorDomain(String url) {
        if (isTopDomain(url)) {
            return false;
        }
        try {
            Matcher matcher = Pattern.compile(RexpConst.RE_TOP, Pattern.CASE_INSENSITIVE).matcher(url);
            matcher.find();
            if (url.equals(matcher.group())) {
                if (!Const.topList.contains(url)) {
                    return true;
                }
            } else {
                String newUrl = url.substring(0, url.indexOf(matcher.group()));
                if (!newUrl.contains(Const.STR_DOT)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 验证是否为未维护顶级域名
     *
     * @param url
     * @return
     */
    public static Boolean isOtherDomain(String url) {
        if (isTopDomain(url)) {
            return false;
        }
        Matcher matcher = Pattern.compile(RexpConst.RE_TOP, Pattern.CASE_INSENSITIVE).matcher(url);
        matcher.find();
        try {
            matcher.group();
        } catch (Exception e) {
            log.error(url + "顶级域名未维护！");
            return true;
        }
        return false;
    }

    /**
     * 获取一级域名
     *
     * @param url
     * @return
     */
    public static String getMajorDomain(String url) {
        if (isTopDomain(url)) {
            return Const.STR_EMPTY;
        }
        Matcher matcher = Pattern.compile(RexpConst.RE_TOP, Pattern.CASE_INSENSITIVE).matcher(url);
        matcher.find();
        if (url.equals(matcher.group())) {
            if (!Const.topList.contains(url)) {
                return url;
            }
        } else {
            String newUrl = url.substring(0, url.indexOf(matcher.group()));
            if (!newUrl.contains(Const.STR_DOT)) {
                return url;
            } else {
                return newUrl.substring(newUrl.lastIndexOf(Const.STR_DOT) + 1) + Const.STR_DOT + matcher.group();
            }
        }
        return Const.STR_EMPTY;
    }

    /**
     * 验证是否为一级域名
     *
     * @param url
     * @return
     */
    public static Boolean isTopDomain(String url) {
        return Const.topList.contains(url);
    }

}
