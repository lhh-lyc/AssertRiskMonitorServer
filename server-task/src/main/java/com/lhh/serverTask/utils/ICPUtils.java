package com.lhh.serverTask.utils;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.utils.MD5;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ICP工具
 */
@Slf4j
public class ICPUtils {

    public static void main(String[] args) {
        try {
            for (int i=0;i<1500;i++) {
                System.out.println(i);
                System.out.println(getCompany("njgdkyhb.com"));
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCompany(String domain) throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        long timestamp = System.currentTimeMillis();
        Boolean flg = false;
        Integer requestNum = Const.INTEGER_0;
        Document doc1 = null;
        Document doc2 = null;
        while(!flg && requestNum < Const.INTEGER_3) {
            requestNum++;
            String token = Const.STR_EMPTY;
            try {
                doc1 = Jsoup.connect("https://hlwicpfwc.miit.gov.cn/icpproject_query/api/auth")
                        .sslSocketFactory(sslContext.getSocketFactory())
                        .data("authKey", MD5.encryptPwdFirst("testtest" + timestamp))
                        .data("timeStamp", String.valueOf(timestamp))
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8").ignoreContentType(true)
                        .referrer("https://beian.miit.gov.cn")
                        .timeout(5000).post();
                token = JSONObject.parseObject(doc1.text()).getJSONObject("params").getString("bussiness");
            } catch (IOException e) {
                log.error("出错", e);
            }
            try {
                doc2 = Jsoup.connect("https://hlwicpfwc.miit.gov.cn/icpproject_query/api/icpAbbreviateInfo/queryByCondition")
                        .sslSocketFactory(sslContext.getSocketFactory())
                        .header("Content-Type", "application/json")
                        .header("token", token)
                        .referrer("https://beian.miit.gov.cn")
                        .requestBody("{\"pageNum\":\"" + 1 + "\",\"pageSize\":\"" + 1 + "\",\"unitName\":\"" + domain + "\"}")
                        .timeout(5000).post();
                flg = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String company = Const.STR_CROSSBAR;
        if (doc2 != null) {
            JSONObject obj = JSONObject.parseObject(doc2.text());
            if ("false".equals(MapUtil.getStr(obj, "success"))){
                return company;
            }
            List<Map> list = JSON.parseArray(JSONObject.parseObject(doc2.text()).getJSONObject("params").getString("list"), Map.class);
            if (!CollectionUtils.isEmpty(list)) {
                company = MapUtil.getStr(list.get(0), "unitName");
            }
        }
        return StringUtils.isEmpty(company) ? Const.STR_CROSSBAR : company;
    }

}
