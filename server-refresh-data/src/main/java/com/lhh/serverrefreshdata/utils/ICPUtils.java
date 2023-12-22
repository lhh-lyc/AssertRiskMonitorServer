package com.lhh.serverrefreshdata.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.utils.MD5;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ICP工具
 */
@Slf4j
@Component
public class ICPUtils {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public String getCompanyUseless(String domain) throws Exception {
        String queryFlg = stringRedisTemplate.opsForValue().get(CacheConst.REDIS_COMPANY_QUERY);
        if (!StringUtils.isEmpty(queryFlg)) {
            Thread.sleep(60000);
            getCompanyUseless(domain);
        }
        stringRedisTemplate.opsForValue().set(CacheConst.REDIS_COMPANY_QUERY, Const.STR_1, 60L, TimeUnit.SECONDS);
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

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        long timestamp = System.currentTimeMillis();
        Boolean flg = false;
        Integer requestNum = Const.INTEGER_0;
        Document doc1 = null;
        Document doc2 = null;
        Long s = System.currentTimeMillis();
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
                        .header("Cookie", "__jsluid_s=2b79f2531980acb42b63c04e14e70f60")
                        .referrer("https://beian.miit.gov.cn")
                        .requestBody("{\"pageNum\":\"" + 1 + "\",\"pageSize\":\"" + 1 + "\",\"unitName\":\"" + domain + "\",\"serviceType\":\"" + 1 + "\"}")
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

    public static void main(String[] args) {
        /*Connection connection= Jsoup.connect("https://www.baidu.com");
        *//*
         * 这个地方的proxy 参数第一个是String 类型 用于传入 代理Ip地址  第二个参数是int类型用于传入 Ip端口号
         * *//*
        Connection.Response execute = null;
        try {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS,
                    new InetSocketAddress("111.230.26.98", 1080));
            execute = connection.method(Connection.Method.GET).timeout(2000*1000).proxy(proxy).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i = execute.statusCode();
        System.out.println("响应状态码为:"+i);*/
    }

    public String getCompany(String domain) {
        if (StringUtils.isEmpty(domain)) {
            return Const.STR_CROSSBAR;
        }
        HashMap<String, Object> params = new HashMap();
        params.put("type", "1");
        params.put("items[]","24");
        params.put("domain", domain);
        String data = HttpRequest.post("http://www.jucha.com/item/search")
                .contentType("application/x-www-form-urlencoded;")
                .form(params).execute().body();

        String company = Const.STR_CROSSBAR;
        JSONObject jsonObject = JSONObject.parseObject(data);
        if (Const.INTEGER_1.equals(MapUtil.getInt(jsonObject, "code"))) {
            //获取第一层
            JSONObject data1 = jsonObject.getJSONObject("data");
            //获取第一层
            JSONObject beian = data1.getJSONObject("beian");
            if (Const.INTEGER_200.equals(MapUtil.getInt(beian, "err_code"))) {
                //获取第二层
                JSONObject data2 = beian.getJSONObject("data");
                if (Const.INTEGER_1.equals(MapUtil.getInt(jsonObject, "code"))) {
                    //获取第三层,备案数据
                    JSONObject info = data2.getJSONObject("data");
                    company = MapUtil.getStr(info, "mc");
                }
            }
        }
        return StringUtils.isEmpty(company) ? Const.STR_CROSSBAR : company;
    }

}
