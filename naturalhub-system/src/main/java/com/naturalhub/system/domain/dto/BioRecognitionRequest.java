package com.naturalhub.system.domain.dto;

/**
 * 生物识别请求DTO
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
public class BioRecognitionRequest {
    
    /** 图片Base64编码（不含前缀，与url二选一） */
    private String image;
    
    /** 图片URL（与image二选一） */
    private String url;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "BioRecognitionRequest{" +
                "image=" + (image != null ? "***" : "null") +
                ", url='" + url + '\'' +
                '}';
    }
}
