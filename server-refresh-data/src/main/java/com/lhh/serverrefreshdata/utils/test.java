package com.lhh.serverrefreshdata.utils;

import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.utils.MD5;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class test {

    public static void main(String[] args) {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        long timestamp = System.currentTimeMillis();
        Boolean flg = false;
        Integer requestNum = Const.INTEGER_0;
        Document doc1 = null;
        Document doc2 = null;
        String token = Const.STR_EMPTY;
        try {
            doc1 = Jsoup.connect("https://hlwicpfwc.miit.gov.cn/icpproject_query/api/auth")
                    .proxy("114.254.131.31", 1080)
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
        log.info("token:" + token);
    }

}
