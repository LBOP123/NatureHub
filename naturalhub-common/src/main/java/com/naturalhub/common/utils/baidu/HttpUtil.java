package com.naturalhub.common.utils.baidu;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP工具类
 * 用于发送HTTP请求
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
public class HttpUtil {
    
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    
    /** 连接超时时间（毫秒） */
    private static final int CONNECT_TIMEOUT = 10000;
    
    /** 读取超时时间（毫秒） */
    private static final int READ_TIMEOUT = 30000;
    
    /**
     * 发送GET请求
     * 
     * @param urlString 请求URL
     * @param params 请求参数
     * @return 响应内容
     * @throws Exception 异常
     */
    public static String get(String urlString, Map<String, String> params) throws Exception {
        // 构建URL参数
        if (params != null && !params.isEmpty()) {
            StringBuilder sb = new StringBuilder(urlString);
            sb.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=")
                  .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                  .append("&");
            }
            urlString = sb.substring(0, sb.length() - 1);
        }
        
        log.debug("GET请求URL: {}", urlString);
        
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        
        return readResponse(conn);
    }
    
    /**
     * 发送POST请求（表单格式）
     * 
     * @param urlString 请求URL
     * @param params 请求参数
     * @return 响应内容
     * @throws Exception 异常
     */
    public static String post(String urlString, Map<String, String> params) throws Exception {
        log.debug("POST请求URL: {}", urlString);
        
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        // 构建请求体
        if (params != null && !params.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=")
                  .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                  .append("&");
            }
            String body = sb.substring(0, sb.length() - 1);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        }
        
        return readResponse(conn);
    }
    
    /**
     * 发送POST请求（JSON格式）
     * 
     * @param urlString 请求URL
     * @param jsonBody JSON请求体
     * @return 响应内容
     * @throws Exception 异常
     */
    public static String postJson(String urlString, String jsonBody) throws Exception {
        log.debug("POST JSON请求URL: {}", urlString);
        
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
        
        return readResponse(conn);
    }
    
    /**
     * 读取响应内容
     * 
     * @param conn HTTP连接
     * @return 响应内容
     * @throws Exception 异常
     */
    private static String readResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        log.debug("响应状态码: {}", responseCode);
        
        BufferedReader reader;
        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        String result = response.toString();
        log.debug("响应内容: {}", result);
        
        return result;
    }
}
