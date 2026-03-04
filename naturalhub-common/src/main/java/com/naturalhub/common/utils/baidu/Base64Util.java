package com.naturalhub.common.utils.baidu;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

/**
 * Base64工具类
 * 用于图片的Base64编码
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
public class Base64Util {
    
    /**
     * 将图片文件转换为Base64字符串
     * 
     * @param imagePath 图片文件路径
     * @return Base64字符串（不含前缀）
     * @throws Exception 异常
     */
    public static String encodeImageToBase64(String imagePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(imagePath)) {
            return encodeStreamToBase64(fis);
        }
    }
    
    /**
     * 将图片URL转换为Base64字符串
     * 
     * @param imageUrl 图片URL
     * @return Base64字符串（不含前缀）
     * @throws Exception 异常
     */
    public static String encodeUrlToBase64(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        
        try (InputStream is = conn.getInputStream()) {
            return encodeStreamToBase64(is);
        }
    }
    
    /**
     * 将输入流转换为Base64字符串
     * 
     * @param is 输入流
     * @return Base64字符串（不含前缀）
     * @throws Exception 异常
     */
    public static String encodeStreamToBase64(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    /**
     * 将Base64字符串转换为字节数组
     * 
     * @param base64String Base64字符串
     * @return 字节数组
     */
    public static byte[] decodeBase64(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }
    
    /**
     * 去除Base64字符串的前缀（如：data:image/png;base64,）
     * 
     * @param base64String 带前缀的Base64字符串
     * @return 纯Base64字符串
     */
    public static String removeBase64Prefix(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return base64String;
        }
        
        int index = base64String.indexOf(",");
        if (index > 0) {
            return base64String.substring(index + 1);
        }
        
        return base64String;
    }
    
    /**
     * 验证Base64字符串是否有效
     * 
     * @param base64String Base64字符串
     * @return true-有效，false-无效
     */
    public static boolean isValidBase64(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return false;
        }
        
        try {
            Base64.getDecoder().decode(removeBase64Prefix(base64String));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取Base64编码后的大小（字节）
     * 
     * @param base64String Base64字符串
     * @return 大小（字节）
     */
    public static long getBase64Size(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return 0;
        }
        
        String pure = removeBase64Prefix(base64String);
        // Base64编码后大小约为原始大小的4/3
        return (long) (pure.length() * 0.75);
    }
}
