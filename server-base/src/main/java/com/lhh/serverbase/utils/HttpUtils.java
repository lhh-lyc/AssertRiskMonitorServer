package com.lhh.serverbase.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.common.constant.Const;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

    public static JSONObject httpGet(String url, Map<String, String> headerMap, Map<String, String> params){
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

    public static String getDomainUnit(String domain){
        String unit = Const.STR_EMPTY;
        JSONObject data = null;
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] encode = encoder.encode(domain.getBytes());
            JSONObject obj = httpGet("https://www.mxnzp.com/api/beian/search",
                    new HashMap<String, String>(){{put("app_id", "ytiuiclnnaoshorw");put("app_secret", "OWVLYVJSZnR3TkxaMTlJWlJHUmJtQT09");}},
                    new HashMap<String, String>(){{put("domain", new String(encode));}});
            String dataStr = MapUtil.getStr(obj, "data");
            data = JSONObject.parseObject(dataStr);
            unit = MapUtil.getStr(data, "unit");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unit;
    }

    public static void main(String[] args) {
        String obj = getDomainUnit("hb.com");
        System.out.println(obj);
    }

}
