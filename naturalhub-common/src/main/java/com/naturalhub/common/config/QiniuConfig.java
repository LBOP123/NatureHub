package com.naturalhub.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 七牛云配置类
 * 
 * @author naturalhub
 * @date 2025-03-05
 */
@Component
@ConfigurationProperties(prefix = "qiniu")
public class QiniuConfig {
    
    /** AccessKey */
    private String accessKey;
    
    /** SecretKey */
    private String secretKey;
    
    /** 存储空间名称 */
    private String bucket;
    
    /** 外链域名 */
    private String domain;
    
    /** 上传区域（华东、华北、华南等） */
    private String zone;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}
