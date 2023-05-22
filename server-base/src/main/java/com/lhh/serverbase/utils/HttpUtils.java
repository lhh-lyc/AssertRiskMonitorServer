package com.lhh.serverbase.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.common.constant.Const;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    public static JSONObject httpPost(String domain){
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encode = encoder.encode(domain.getBytes());
        JSONObject obj = httpGet("https://www.mxnzp.com/api/beian/search",
                new HashMap<String, String>(){{put("app_id", "ytiuiclnnaoshorw");put("app_secret", "OWVLYVJSZnR3TkxaMTlJWlJHUmJtQT09");}},
                new HashMap<String, String>(){{put("domain", new String(encode));}});
        return obj;
    }

    public static String getDomainUnit(String domain){
        System.out.println(domain + "-公司名开始查询");
        String unit = Const.STR_EMPTY;
        JSONObject data = null;
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] encode = encoder.encode(domain.getBytes());
            JSONObject obj = httpGet("https://www.mxnzp.com/api/beian/search",
                    new HashMap<String, String>(){{put("app_id", "ytiuiclnnaoshorw");put("app_secret", "OWVLYVJSZnR3TkxaMTlJWlJHUmJtQT09");}},
                    new HashMap<String, String>(){{put("domain", new String(encode));}});
            Integer code = MapUtil.getInt(obj, "code");
            while (code.equals(101)) {
                Thread.sleep(1000);
                System.out.println(domain + "-公司名查询等待中。。。。。。。");
                obj = httpGet("https://www.mxnzp.com/api/beian/search",
                        new HashMap<String, String>(){{put("app_id", "ytiuiclnnaoshorw");put("app_secret", "OWVLYVJSZnR3TkxaMTlJWlJHUmJtQT09");}},
                        new HashMap<String, String>(){{put("domain", new String(encode));}});
                code = MapUtil.getInt(obj, "code");
            }
            String dataStr = MapUtil.getStr(obj, "data");
            data = JSONObject.parseObject(dataStr);
            unit = MapUtil.getStr(data, "unit");
            System.out.println(domain + "-公司名查询完成：" + unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unit;
    }

    public static void main(String[] args) {
        List<String> list = Arrays.asList("zxbigdata.caict.ac.cn","sdk.caict.ac.cn","bike.caict.ac.cn","zxid.caict.ac.cn","aimd.caict.ac.cn","ii-resource.caict.ac.cn","testmcrp.caict.ac.cn","report.caict.ac.cn","ictp.caict.ac.cn","3im.caict.ac.cn","eps.caict.ac.cn","maildx.caict.ac.cn","xtwk.caict.ac.cn","insurance-iov.caict.ac.cn","caict.ac.cn","www2.caict.ac.cn","yds.caict.ac.cn","gw.caict.ac.cn","chinatcc.caict.ac.cn","finance-iov.caict.ac.cn","mcrp.caict.ac.cn","app.caict.ac.cn","mcrp.caict.ac.cn","ageing.caict.ac.cn","v2x.caict.ac.cn","xc.caict.ac.cn","cepn-iov.caict.ac.cn","www.caict.ac.cn","healthcare.caict.ac.cn","healthcare.caict.ac.cn","healthcare.caict.ac.cn","healthcare.caict.ac.cn","healthcare.caict.ac.cn","healthcare.caict.ac.cn","healthcare.caict.ac.cn","healthcare.caict.ac.cn","healthcare.caict.ac.cn","healthcare.caict.ac.cn","hgjg.caict.ac.cn","bd-iov.caict.ac.cn","bd-iov.caict.ac.cn","wk.caict.ac.cn","wk.caict.ac.cn","wk.caict.ac.cn","wk.caict.ac.cn","tsn.caict.ac.cn","mail.caict.ac.cn","mail.caict.ac.cn","k1.caict.ac.cn","spf1.caict.ac.cn","wmail.caict.ac.cn","np.caict.ac.cn","qyfw.caict.ac.cn","diaocha.caict.ac.cn","maillt.caict.ac.cn","spim.caict.ac.cn","dns2.caict.ac.cn");
        for (String s : list) {
            String obj = getDomainUnit(s);
        }
    }

}
