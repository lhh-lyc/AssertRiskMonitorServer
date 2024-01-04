package com.lhh.servermonitor.utils;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.entity.CmsJsonEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.servermonitor.dao.CmsJsonDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义httpx逻辑，查询url、title
 */
@Slf4j
@Service
public class HttpscanCustomizeUtils {

    @Autowired
    CmsJsonDao cmsJsonDao;

    public String getUrlCms(String toolDir, String url) {
        Map<String, Object> responseMap = urlConn(url);
        String cms = getAllCms(toolDir, url, responseMap);
        return cms;
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
        Map<String, List<String>> headers = new HashMap<>();
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

    public String getAllCms(String toolDir, String cmsUrl, Map<String, Object> responseMap) {
        String hashCms = getHashCms(toolDir, cmsUrl);
        List<String> keyWordCms = getKeyWordCms(responseMap);
        List<String> regulaCms = getRegulaCms(responseMap);
        List<String> list = new ArrayList<>();
        list.add(hashCms);
        list.addAll(keyWordCms);
        list.addAll(regulaCms);
        list = list.stream().filter(s->!StringUtils.isEmpty(s)).distinct().collect(Collectors.toList());
        String cms = CollectionUtils.isEmpty(list) ? Const.STR_CROSSBAR : String.join(Const.STR_COMMA, list);
        return cms;
    }

    public String getHashCms(String toolDir, String cmsUrl) {
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
                    List<CmsJsonEntity> cmsJsonList = cmsJsonDao.queryList(new HashMap<String, Object>(){{put("method", "faviconhash");}});
                    Map<String, String> cmsJsonMap = cmsJsonList.stream().collect(Collectors.toMap(
                            CmsJsonEntity::getKeyword, CmsJsonEntity::getCms, (key1, key2) -> key1));
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

    public List<String> getKeyWordCms(Map<String, Object> responseMap) {
        List<String> cmsList = new ArrayList<>();
        // todo 内存隐患
        List<CmsJsonEntity> cmsJsonList = cmsJsonDao.queryList(new HashMap<String, Object>(){{put("method", "keyword");}});
        if (!CollectionUtils.isEmpty(cmsJsonList)) {
            for (CmsJsonEntity json : cmsJsonList) {
                List<String> keyWordList = new ArrayList<>(Arrays.asList(json.getKeyword().split(Const.STR_COMMA)));
                //一个规则有一组多个keyword，所有keyword都被包含才算识别到该规则
                Boolean flg = true;
                for (String key : keyWordList) {
                    if (!responseMap.containsKey(json.getLocation()) || !MapUtil.getStr(responseMap, json.getLocation()).contains(key)) {
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

    public static List<String> getRegulaCms(Map<String, Object> responseMap) {
        List<String> cmsList = new ArrayList<>();
        return cmsList;
    }

}
