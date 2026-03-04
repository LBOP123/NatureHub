package com.naturalhub.common.utils.baidu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token缓存工具类
 * 使用内存缓存，支持过期时间
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
public class TokenCacheUtil {
    
    /** 缓存容器 */
    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();
    
    /** 缓存键 */
    private static final String TOKEN_KEY = "baidu_ai_token";
    
    /**
     * 缓存实体
     */
    private static class CacheEntry {
        private String value;
        private long expireTime;
        
        public CacheEntry(String value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    /**
     * 存储Token
     * 
     * @param token Token值
     * @param expireSeconds 过期时间（秒）
     */
    public static void setToken(String token, long expireSeconds) {
        long expireTime = System.currentTimeMillis() + (expireSeconds * 1000);
        CACHE.put(TOKEN_KEY, new CacheEntry(token, expireTime));
    }
    
    /**
     * 获取Token
     * 
     * @return Token值，如果不存在或已过期返回null
     */
    public static String getToken() {
        CacheEntry entry = CACHE.get(TOKEN_KEY);
        if (entry == null || entry.isExpired()) {
            CACHE.remove(TOKEN_KEY);
            return null;
        }
        return entry.getValue();
    }
    
    /**
     * 清除Token
     */
    public static void clearToken() {
        CACHE.remove(TOKEN_KEY);
    }
    
    /**
     * 检查Token是否存在且有效
     * 
     * @return true-有效，false-无效
     */
    public static boolean hasValidToken() {
        return getToken() != null;
    }
}
