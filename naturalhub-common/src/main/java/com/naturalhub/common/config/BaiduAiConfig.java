package com.naturalhub.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 百度AI配置类
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
@Component
@ConfigurationProperties(prefix = "baidu.ai")
public class BaiduAiConfig {
    
    /** API Key */
    private String apiKey;
    
    /** Secret Key */
    private String secretKey;
    
    /** Token获取URL */
    private String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";
    
    /** 通用物体识别URL */
    private String generalUrl = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general";
    
    /** 植物识别URL */
    private String plantUrl = "https://aip.baidubce.com/rest/2.0/image-classify/v1/plant";
    
    /** 动物识别URL */
    private String animalUrl = "https://aip.baidubce.com/rest/2.0/image-classify/v1/animal";
    
    /** Token缓存时间（秒），默认25天 */
    private Long tokenExpireTime = 2160000L;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getGeneralUrl() {
        return generalUrl;
    }

    public void setGeneralUrl(String generalUrl) {
        this.generalUrl = generalUrl;
    }

    public String getPlantUrl() {
        return plantUrl;
    }

    public void setPlantUrl(String plantUrl) {
        this.plantUrl = plantUrl;
    }

    public String getAnimalUrl() {
        return animalUrl;
    }

    public void setAnimalUrl(String animalUrl) {
        this.animalUrl = animalUrl;
    }

    public Long getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(Long tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
    }
}
