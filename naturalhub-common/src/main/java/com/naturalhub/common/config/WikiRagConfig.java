package com.naturalhub.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Wiki知识库 + 千问AI 配置类
 *
 * @author naturalhub
 */
@Component
@ConfigurationProperties(prefix = "wiki")
public class WikiRagConfig {

    /** Wiki GraphQL 地址 */
    private String url;

    /** Wiki API Key (JWT) */
    private String key;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
