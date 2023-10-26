package com.lhh.serverbase.utils;

import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.constant.RexpConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
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
        String domainName = "";
        String b = "";
        Boolean flag1 = startWithSchedule("53% (1633/3063), 34s");
        System.out.println(flag1);
        Boolean flag2 = startWithSchedule("");
        System.out.println(flag2);
        Boolean flag3 = startWithSchedule("004 10-24 11:37:16 nacos-default-password HIGH http://18.166.253.67:8848/nacos/v1/auth/users/login ");
        System.out.println(flag3);
//        flag = isMajorDomain("freetyst.vip.migu.cn");
//        System.out.println(flag);
//        domainName = "baidu.com";
//        b = getMajorDomain(domainName);
//        System.out.println(b);
//        domainName = "culanjing.com.cn";
//        b = getMajorDomain(domainName);
//        flag = isMajorDomain(domainName);
//        System.out.println(b);
//        System.out.println(flag);
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
            log.info(url + "顶级域名未维护！");
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
        if (isIP(url)) {
            return url;
        }
        if (isTopDomain(url)) {
            return Const.STR_EMPTY;
        }
        try {
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
                    return newUrl.substring(newUrl.lastIndexOf(Const.STR_DOT) + 1) + matcher.group();
                }
            }
        } catch (IllegalStateException e) {
            log.error(url + "包含未知顶级域名，不预解析！");
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

    /**
     * 验证是否为udp端口
     *
     * @param domain
     * @return
     */
    public static boolean isUdpPort(String domain) {
        if (domain.matches(RexpConst.uPortRex)) {
            return true;
        }
        return false;
    }

    /**
     * 验证是否为tcp端口
     *
     * @param domain
     * @return
     */
    public static boolean isTcpPort(String domain) {
        if (domain.matches(RexpConst.tPortRex)) {
            return true;
        }
        return false;
    }

    /**
     * 获取title
     *
     * @param domain
     * @return
     */
    public static String getTitle(String domain) {
        Pattern pattern = Pattern.compile(RexpConst.titleRex);
        Matcher matcher = pattern.matcher(domain);
        String result = Const.STR_EMPTY;
        if (matcher.find()) {
            result = matcher.group(1);
        }
        result = StringUtils.isEmpty(result) ? Const.STR_CROSSBAR : result;
        return result;
    }

    /**
     * 请求脚本返回，去除颜色代码
     *
     * @param str
     * @return
     */
    public static String removeColor(String str) {
        Pattern pattern = Pattern.compile(RexpConst.colorRex);
        Matcher matcher = pattern.matcher(str);
        // 使用空字符串替换颜色代码
        String result = matcher.replaceAll("");
        return result;
    }

    /**
     * 1-100%匹配字符串开头
     *
     * @param str
     * @return
     */
    public static Boolean startWithSchedule(String str) {
        if (!StringUtils.isEmpty(str)) {
            List<String> list = Arrays.asList(str.split(Const.STR_BLANK));
            if (!CollectionUtils.isEmpty(list)) {
                if (list.get(0).matches(RexpConst.scheduleRex)) {
                    return true;
                }
            }
        }
        return false;
    }

}
