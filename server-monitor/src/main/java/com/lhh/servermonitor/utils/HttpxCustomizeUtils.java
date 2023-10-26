package com.lhh.servermonitor.utils;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.CmsJsonEntity;
import com.lhh.serverbase.entity.SshResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义httpx逻辑，查询url、title
 */
@Slf4j
public class HttpxCustomizeUtils {

    public static Map<String, String> getUrlMap(StringRedisTemplate stringRedisTemplate, String toolDir, String initialUrl) throws IOException {
        Integer statusCode;
        Map<String, Object> firstHttp = new HashMap<>();
        String url = "https://" + initialUrl;
        Map<String, Object> firstHttps = urlConn(url);
        statusCode = MapUtil.getInt(firstHttps, "statusCode");
        String body = MapUtil.getStr(firstHttps, "body");
        if (Const.INTEGER_0.equals(statusCode) || (statusCode >= Const.INTEGER_300 && statusCode <= Const.INTEGER_400)) {
            firstHttp = urlConn("http://" + initialUrl);
            statusCode = MapUtil.getInt(firstHttp, "statusCode");
            if (!Const.INTEGER_0.equals(statusCode)) {
                url = "http://" + initialUrl;
                body = MapUtil.getStr(firstHttp, "body");
            }
        }
        // 使用 Jsoup 解析 HTML 文档
        Document doc = Jsoup.parse(body);
        String title = doc.selectFirst("title") == null ? Const.STR_CROSSBAR : doc.selectFirst("title").text();

        if (title.contains("�") || title.contains("\uDBA6\uDD33")) {
            title = Const.STR_CROSSBAR;
            log.info("乱码");
        }
        String cms = getCms(stringRedisTemplate, toolDir, firstHttp, firstHttps);
        Map<String, String> result = new HashMap<>();
        result.put("url", url == null ? Const.STR_CROSSBAR : url);
        result.put("title", title);
        result.put("cms", cms);
        return result;
    }

    public static String getCms(StringRedisTemplate stringRedisTemplate, String toolDir, Map<String, Object> firstHttp, Map<String, Object> firstHttps){
        String cms;
        Integer statusCode;
        statusCode = MapUtil.getInt(firstHttps, "statusCode");
        if (Const.INTEGER_0.equals(statusCode) || (statusCode >= Const.INTEGER_300 && statusCode <= Const.INTEGER_400)){
            cms = getAllCms(stringRedisTemplate, toolDir, MapUtil.getStr(firstHttp, "url"), firstHttp);
        } else {
            cms = getAllCms(stringRedisTemplate, toolDir, MapUtil.getStr(firstHttps, "url"), firstHttps);
        }
        return cms;
    }

    public static void main(String[] args) {
        urlConn("https://www.njgdkyhb.com:7001");
    }

    /**
     * 用这种http请求方式，是因为即使请求404，也能拿到reponse的dom字符串
     * @param httpUrl
     * @return
     */
    public static Map<String, Object> urlConn(String httpUrl){
        HttpURLConnection connection = null;
        Map<String, Object> result = new HashMap<>();
        int statusCode = 0;
        StringBuilder response = null;
        Map<String, java.util.List<String>> headers = new HashMap<>();
        try {
            // 创建信任所有证书的TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }
                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };
            // 忽略对SSL证书的验证
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("Cookie", "rememberMe=test");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            statusCode = connection.getResponseCode();
            BufferedReader reader;
            if (statusCode >= 200 && statusCode < 400) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            headers = connection.getHeaderFields();
            String line;
            response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        } catch (Exception e) {
            result.put("url", httpUrl);
            log.error(httpUrl + "请求失败");
        } finally {
            result.put("url", httpUrl);
            result.put("statusCode", statusCode);
            result.put("body", response == null ? Const.STR_EMPTY : response.toString());
            result.put("header", JSON.toJSONString(headers));
            connection.disconnect();
        }
        return result;
    }

    public static String getAllCms(StringRedisTemplate stringRedisTemplate, String toolDir, String cmsUrl, Map<String, Object> responseMap) {
        String hashCms = getHashCms(stringRedisTemplate, toolDir, cmsUrl);
        List<String> keyWordCms = getKeyWordCms(stringRedisTemplate, responseMap);
        List<String> regulaCms = getRegulaCms(stringRedisTemplate, responseMap);
        List<String> list = new ArrayList<>();
        list.add(hashCms);
        list.addAll(keyWordCms);
        list.addAll(regulaCms);
        list = list.stream().filter(s->!StringUtils.isEmpty(s)).distinct().collect(Collectors.toList());
        String cms = CollectionUtils.isEmpty(list) ? Const.STR_CROSSBAR : String.join(Const.STR_COMMA, list);
        return cms;
    }

    public static String getHashCms(StringRedisTemplate stringRedisTemplate, String toolDir, String cmsUrl) {
        String cms = Const.STR_EMPTY;
        Integer hash;
        if (!Const.STR_CROSSBAR.equals(cmsUrl)) {
            log.info("开始请求cmsUrl：" + cmsUrl);
            String cmd = String.format(Const.STR_FAVICON_HASH, toolDir, cmsUrl);
            SshResponse response = null;
            try {
                response = ExecUtil.runCommand(cmd);
                try {
                    hash = CollectionUtils.isEmpty(response.getOutList()) ? Const.INTEGER_0 : Integer.valueOf(response.getOutList().get(0));
                    String cmsJsonMapStr = stringRedisTemplate.opsForValue().get(CacheConst.REDIS_CMS_JSON_MAP);
                    Map<String,String> cmsJsonMap = StringUtils.isEmpty(cmsJsonMapStr) ? new HashMap<>() : JSONObject.parseObject(cmsJsonMapStr, Map.class);
                    if (cmsJsonMap.containsKey(String.valueOf(hash))) {
                        cms = cmsJsonMap.get(String.valueOf(hash));
                    }
                } catch (NumberFormatException e) {
                    log.error("cmsUrl请求的返回hash不规范", e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cms;
    }

    public static List<String> getKeyWordCms(StringRedisTemplate stringRedisTemplate, Map<String, Object> responseMap) {
        List<String> cmsList = new ArrayList<>();
        String cmsJsonListStr = stringRedisTemplate.opsForValue().get(CacheConst.REDIS_CMS_JSON_LIST);
        List<CmsJsonEntity> cmsJsonList = StringUtils.isEmpty(cmsJsonListStr) ? new ArrayList<>() : JSONArray.parseArray(cmsJsonListStr, CmsJsonEntity.class);
        if (!CollectionUtils.isEmpty(cmsJsonList)) {
            for (CmsJsonEntity json : cmsJsonList) {
                //一个规则有一组多个keyword，所有keyword都被包含才算识别到该规则
                Boolean flg = true;
                for (String key : json.getKeywordList()) {
                    if (!MapUtil.getStr(responseMap, json.getLocation()).contains(key)) {
                        flg = false;
                    }
                }
                if (flg) {
                    cmsList.add(json.getCms());
                }
            }
        }
        return cmsList;
    }

    public static List<String> getRegulaCms(StringRedisTemplate stringRedisTemplate, Map<String, Object> responseMap) {
        List<String> cmsList = new ArrayList<>();
        return cmsList;
    }

}
