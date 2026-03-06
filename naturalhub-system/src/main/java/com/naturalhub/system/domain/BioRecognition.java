package com.naturalhub.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 生物识别记录
 */
public class BioRecognition {
    
    private Long id;
    private Long userId;
    private String username;
    private String imageUrl;
    private String recognitionResult;
    private String recognitionType;
    private BigDecimal confidence;
    private String status;
    private String baikeInfo;
    private String allResults;
    private String remark;
    private String ipAddress;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;



    public String getAllResults() {
        return allResults;
    }

    public void setAllResults(String allResults) {
        this.allResults = allResults;
    }

    public String getBaikeInfo() {
        return baikeInfo;
    }

    public void setBaikeInfo(String baikeInfo) {
        this.baikeInfo = baikeInfo;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getRecognitionResult() {
        return recognitionResult;
    }

    public void setRecognitionResult(String recognitionResult) {
        this.recognitionResult = recognitionResult;
    }

    public String getRecognitionType() {
        return recognitionType;
    }

    public void setRecognitionType(String recognitionType) {
        this.recognitionType = recognitionType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
