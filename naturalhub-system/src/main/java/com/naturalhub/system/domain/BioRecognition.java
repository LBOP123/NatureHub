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
    /** 识别类型 tinyint: 1-植物 2-动物 3-其他 【字典编码：bio_recognition_type】 */
    private Integer recognitionType;
    private BigDecimal confidence;
    /** 识别状态 tinyint: 1-成功 0-失败 【字典编码：bio_recognition_status】 */
    private Integer status;
    private String baikeInfo;
    private String allResults;
    /** 是否已分享 tinyint: 0-否 1-是 【字典编码：nh_yes_no】 */
    private Integer isShared;
    private Long sharedTopicId;
    private String remark;
    private String ipAddress;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // ===== 字典标签转换方法 =====

    /** 获取识别状态中文标签 */
    public String getStatusLabel() {
        if (status == null) return "";
        return status == 1 ? "识别成功" : "识别失败";
    }

    /** 获取识别类型中文标签 */
    public String getRecognitionTypeLabel() {
        if (recognitionType == null) return "";
        switch (recognitionType) {
            case 1: return "植物";
            case 2: return "动物";
            case 3: return "其他生物";
            default: return "";
        }
    }

    // ===== getters / setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getRecognitionResult() { return recognitionResult; }
    public void setRecognitionResult(String recognitionResult) { this.recognitionResult = recognitionResult; }

    public Integer getRecognitionType() { return recognitionType; }
    public void setRecognitionType(Integer recognitionType) { this.recognitionType = recognitionType; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getBaikeInfo() { return baikeInfo; }
    public void setBaikeInfo(String baikeInfo) { this.baikeInfo = baikeInfo; }

    public String getAllResults() { return allResults; }
    public void setAllResults(String allResults) { this.allResults = allResults; }

    public Integer getIsShared() { return isShared; }
    public void setIsShared(Integer isShared) { this.isShared = isShared; }

    public Long getSharedTopicId() { return sharedTopicId; }
    public void setSharedTopicId(Long sharedTopicId) { this.sharedTopicId = sharedTopicId; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
