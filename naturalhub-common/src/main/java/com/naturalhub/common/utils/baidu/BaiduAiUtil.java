package com.naturalhub.common.utils.baidu;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.naturalhub.common.config.BaiduAiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 百度AI工具类
 * 封装百度AI接口调用
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
@Component
public class BaiduAiUtil {
    
    private static final Logger log = LoggerFactory.getLogger(BaiduAiUtil.class);
    
    @Autowired
    private BaiduAiConfig config;
    
    /**
     * 获取Access Token
     * 优先从缓存获取，缓存失效则重新获取
     * 
     * @return Access Token
     * @throws Exception 异常
     */
    public String getAccessToken() throws Exception {
        // 先从缓存获取
        String token = TokenCacheUtil.getToken();
        if (token != null) {
            log.debug("从缓存获取Token: {}", token);
            return token;
        }
        
        // 缓存失效，重新获取
        log.info("Token缓存失效，重新获取...");
        
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials");
        params.put("client_id", config.getApiKey());
        params.put("client_secret", config.getSecretKey());
        
        String response = HttpUtil.get(config.getTokenUrl(), params);
        JSONObject json = JSON.parseObject(response);
        
        if (json.containsKey("error")) {
            String error = json.getString("error");
            String errorDesc = json.getString("error_description");
            throw new Exception("获取Token失败: " + error + " - " + errorDesc);
        }
        
        token = json.getString("access_token");
        Long expiresIn = json.getLong("expires_in");
        
        // 存入缓存，提前5分钟过期
        long cacheTime = expiresIn != null ? expiresIn - 300 : config.getTokenExpireTime();
        TokenCacheUtil.setToken(token, cacheTime);
        
        log.info("Token获取成功，缓存时间: {}秒", cacheTime);
        return token;
    }
    
    /**
     * 通用物体和场景识别
     * 
     * @param image Base64编码的图片（不含前缀）
     * @param url 图片URL（与image二选一）
     * @return 识别结果JSON
     * @throws Exception 异常
     */
    public JSONObject generalRecognition(String image, String url) throws Exception {
        String accessToken = getAccessToken();
        String requestUrl = config.getGeneralUrl() + "?access_token=" + accessToken;
        
        Map<String, String> params = new HashMap<>();
        if (image != null && !image.isEmpty()) {
            // 去除可能的前缀，HttpUtil.post会自动进行URL编码，这里不需要再编码
            String cleanImage = Base64Util.removeBase64Prefix(image);
            params.put("image", cleanImage);
            log.info("使用Base64参数，长度: {}", cleanImage.length());
        } else if (url != null && !url.isEmpty()) {
            params.put("url", url);
            log.info("使用URL参数: {}", url);
        } else {
            throw new IllegalArgumentException("image和url不能同时为空");
        }
        params.put("baike_num", "1");
        
        String response = HttpUtil.post(requestUrl, params);
        JSONObject json = JSON.parseObject(response);
        
        if (json.containsKey("error_code")) {
            Integer errorCode = json.getInteger("error_code");
            String errorMsg = json.getString("error_msg");
            throw new Exception("通用识别失败: " + errorCode + " - " + errorMsg);
        }
        
        return json;
    }
    
    /**
     * 植物识别
     * 
     * @param image Base64编码的图片（不含前缀）
     * @param url 图片URL（与image二选一）
     * @return 识别结果JSON
     * @throws Exception 异常
     */
    public JSONObject plantRecognition(String image, String url) throws Exception {
        String accessToken = getAccessToken();
        String requestUrl = config.getPlantUrl() + "?access_token=" + accessToken;
        
        Map<String, String> params = new HashMap<>();
        if (image != null && !image.isEmpty()) {
            // HttpUtil.post会自动进行URL编码，这里不需要再编码
            String cleanImage = Base64Util.removeBase64Prefix(image);
            params.put("image", cleanImage);
            log.info("使用Base64参数，长度: {}", cleanImage.length());
        } else if (url != null && !url.isEmpty()) {
            params.put("url", url);
            log.info("使用URL参数: {}", url);
        } else {
            throw new IllegalArgumentException("image和url不能同时为空");
        }
        params.put("baike_num", "1");
        
        String response = HttpUtil.post(requestUrl, params);
        JSONObject json = JSON.parseObject(response);
        
        if (json.containsKey("error_code")) {
            Integer errorCode = json.getInteger("error_code");
            String errorMsg = json.getString("error_msg");
            throw new Exception("植物识别失败: " + errorCode + " - " + errorMsg);
        }
        
        return json;
    }
    
    /**
     * 动物识别
     * 
     * @param image Base64编码的图片（不含前缀）
     * @param url 图片URL（与image二选一）
     * @return 识别结果JSON
     * @throws Exception 异常
     */
    public JSONObject animalRecognition(String image, String url) throws Exception {
        String accessToken = getAccessToken();
        String requestUrl = config.getAnimalUrl() + "?access_token=" + accessToken;
        
        Map<String, String> params = new HashMap<>();
        if (image != null && !image.isEmpty()) {
            // HttpUtil.post会自动进行URL编码，这里不需要再编码
            String cleanImage = Base64Util.removeBase64Prefix(image);
            params.put("image", cleanImage);
            log.info("使用Base64参数，长度: {}", cleanImage.length());
        } else if (url != null && !url.isEmpty()) {
            params.put("url", url);
            log.info("使用URL参数: {}", url);
        } else {
            throw new IllegalArgumentException("image和url不能同时为空");
        }
        params.put("baike_num", "1");
        params.put("top_num", "3");
        
        String response = HttpUtil.post(requestUrl, params);
        JSONObject json = JSON.parseObject(response);
        
        if (json.containsKey("error_code")) {
            Integer errorCode = json.getInteger("error_code");
            String errorMsg = json.getString("error_msg");
            throw new Exception("动物识别失败: " + errorCode + " - " + errorMsg);
        }
        
        return json;
    }
}
