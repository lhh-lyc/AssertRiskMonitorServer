package com.lhh.serverbase.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.common.constant.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class HttpUtils {

    public static JSONObject httpGet(String url, Map<String, String> headerMap, Map<String, String> params) {
        String param = Const.STR_EMPTY;
        if (!CollectionUtils.isEmpty(params)) {
            for (String key : params.keySet()) {
                param += key + Const.STR_EQUAL + params.get(key);
            }
        }
        if (!StringUtils.isEmpty(param)) {
            url += Const.STR_QUESTION + param;
        }
        HttpRequest request = HttpRequest.get(url);

        if (!CollectionUtils.isEmpty(headerMap)) {
            for (String key : headerMap.keySet()) {
                request.header(key, headerMap.get(key));
            }
        }
        request.header("app_id", "ytiuiclnnaoshorw");
        request.header("app_secret", "OWVLYVJSZnR3TkxaMTlJWlJHUmJtQT09");
        request.timeout(20000);
        String result2 = request.execute().body();
        JSONObject obj = JSONObject.parseObject(result2);
        return obj;
    }

    public static JSONObject httpPost(String domain) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encode = encoder.encode(domain.getBytes());
        JSONObject obj = httpGet("https://www.mxnzp.com/api/beian/search",
                new HashMap<String, String>() {{
                    put("app_id", "ytiuiclnnaoshorw");
                    put("app_secret", "OWVLYVJSZnR3TkxaMTlJWlJHUmJtQT09");
                }},
                new HashMap<String, String>() {{
                    put("domain", new String(encode));
                }});
        return obj;
    }

    public static String getDomainUnit(String domain) {
        log.info(domain + "-公司名开始查询");
        String unit = Const.STR_EMPTY;
        JSONObject data = null;
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] encode = encoder.encode(domain.getBytes());
            JSONObject obj = httpGet("https://www.mxnzp.com/api/beian/search",
                    new HashMap<String, String>() {{
                        put("app_id", "ytiuiclnnaoshorw");
                        put("app_secret", "OWVLYVJSZnR3TkxaMTlJWlJHUmJtQT09");
                    }},
                    new HashMap<String, String>() {{
                        put("domain", new String(encode));
                    }});
            Integer code = MapUtil.getInt(obj, "code");
            if (code.equals(0)) {
                log.info(domain + ":该域名未备案或已取消备案");
                return Const.STR_EMPTY;
            }
            while (code.equals(101)) {
                log.info(domain + "-公司名查询等待中。。。。。。。");
                Thread.sleep(1500);
                obj = httpGet("https://www.mxnzp.com/api/beian/search",
                        new HashMap<String, String>() {{
                            put("app_id", "ytiuiclnnaoshorw");
                            put("app_secret", "OWVLYVJSZnR3TkxaMTlJWlJHUmJtQT09");
                        }},
                        new HashMap<String, String>() {{
                            put("domain", new String(encode));
                        }});
                code = MapUtil.getInt(obj, "code");
            }
            String dataStr = MapUtil.getStr(obj, "data");
            data = JSONObject.parseObject(dataStr);
            unit = StringUtils.isEmpty(MapUtil.getStr(data, "unit")) ? Const.STR_EMPTY : MapUtil.getStr(data, "unit");
            log.info(domain + "-公司名查询完成：" + unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unit;
    }

}
