package com.lhh.serverbase.utils;

import com.lhh.serverbase.common.constant.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DomainIpUtils {

    /**
     * java代码解析域名ip
     */
    public static List<String> getRandomDomainIpList(String domain) {
        List<String> list = new ArrayList<>();
        try {
            InetAddress[] inetadd = InetAddress.getAllByName(domain);
            //遍历所有的ip并输出
            for (int i = 0; i < inetadd.length; i++) {
                if (!StringUtils.isEmpty(inetadd[i] + Const.STR_EMPTY)) {
                    String ip = (inetadd[i] + Const.STR_EMPTY).split(Const.STR_SLASH)[1];
                    if (RexpUtil.isIP(ip)) {
                        list.add(ip);
                    }
                }
            }
            String ips = CollectionUtils.isEmpty(list) ? Const.STR_EMPTY : String.join(Const.STR_COMMA, list);
            log.info(domain + (CollectionUtils.isEmpty(list) ? "未解析出ip" : "解析ip为：" + ips));
        } catch (UnknownHostException e) {
            list.add(Const.STR_CROSSBAR);
        }
        return list;
    }

    /**
     * java代码解析域名ip
     */
    public static List<String> getDomainIpList(String domain) {
        List<String> list = new ArrayList<>();
        try {
            InetAddress[] inetadd = InetAddress.getAllByName(domain);
            //遍历所有的ip并输出
            for (int i = 0; i < inetadd.length; i++) {
                if (!StringUtils.isEmpty(inetadd[i] + Const.STR_EMPTY)) {
                    String ip = (inetadd[i] + Const.STR_EMPTY).split(Const.STR_SLASH)[1];
                    if (RexpUtil.isIP(ip)) {
                        list.add(ip);
                    }
                }
            }
            String ips = CollectionUtils.isEmpty(list) ? Const.STR_EMPTY : String.join(Const.STR_COMMA, list);
            log.info(domain + (CollectionUtils.isEmpty(list) ? "未解析出ip" : "解析ip为：" + ips));
        } catch (UnknownHostException e) {
            list.add(Const.STR_CROSSBAR);
            log.error(domain + "解析ip异常");
        }
        return list;
    }

}
