package com.naturalhub.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 千问AI 配置类
 *
 * @author naturalhub
 */
@Component
@ConfigurationProperties(prefix = "qwen")
public class QwenConfig {

    /** 千问 API 地址 */
    private String api;

    /** 千问 API Key */
    private String key;

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
