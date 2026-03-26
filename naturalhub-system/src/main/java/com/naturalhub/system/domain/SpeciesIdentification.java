package com.naturalhub.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.naturalhub.common.annotation.Excel;
import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 物种鉴定求助对象 nh_species_identification
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
public class SpeciesIdentification extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 鉴定ID */
    private Long identificationId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 标题 */
    @Excel(name = "标题")
    private String title;

    /** 详细描述 */
    @Excel(name = "详细描述")
    private String description;

    /** 图片URL列表 */
    private String images;

    /** 观察时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "观察时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date observationTime;

    /** 观察地点 */
    @Excel(name = "观察地点")
    private String location;

    /** 纬度 */
    @Excel(name = "纬度")
    private BigDecimal latitude;

    /** 经度 */
    @Excel(name = "经度")
    private BigDecimal longitude;

    /** 生境描述 */
    @Excel(name = "生境描述")
    private String habitat;

    /** 特征描述 */
    @Excel(name = "特征描述")
    private String features;

    /** 状态【字典编码：nh_identification_status】0=未鉴定,1=鉴定中,2=已鉴定 */
    @Excel(name = "状态", readConverterExp = "0=未鉴定,1=鉴定中,2=已鉴定")
    private Integer status;

    /** 最佳答案ID */
    private Long bestAnswerId;

    /** 回答数量 */
    @Excel(name = "回答数量")
    private Integer answerCount;

    /** 浏览次数 */
    @Excel(name = "浏览次数")
    private Integer viewCount;

    /** 是否已分享到社群【字典编码：nh_yes_no】0=否,1=是 */
    @Excel(name = "是否已分享", readConverterExp = "0=否,1=是")
    private Integer isShared;

    /** 关联的社群话题ID */
    private Long sharedTopicId;

    /** 审核状态【字典编码：nh_audit_status】0=草稿,1=待审核,2=已通过,3=已驳回 */
    @Excel(name = "审核状态", readConverterExp = "0=草稿,1=待审核,2=已通过,3=已驳回")
    private Integer auditStatus;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核人 */
    private String auditBy;

    /** 审核备注 */
    private String auditRemark;

    /** 删除标志 */
    private String delFlag;

    /** 分享到社群的话题ID */
    private Long topicId;

    /** AI识别的物种名称 */
    private String aiSpeciesName;

    /** AI识别的置信分数 */
    private Double aiScore;

    /** AI识别类型（animal/plant） */
    private String aiType;

    /** 百度百科 URL */
    private String baikeUrl;

    /** 百科图片 URL */
    private String baikeImageUrl;

    /** 百科描述 */
    private String baikeDescription;

    /** 同意总积分 */
    private Integer voteAgreeScore;

    /** 不同意总积分 */
    private Integer voteDisagreeScore;

    /** 投票状态：0=未开始, 1=进行中, 2=已结束 */
    private String voteStatus;

    /** 最终投票结果：0=同意, 1=不同意, 2=未确定 */
    private String voteResult;

    // getter 和 setter
    public String getAiSpeciesName() {
        return aiSpeciesName;
    }

    public void setAiSpeciesName(String aiSpeciesName) {
        this.aiSpeciesName = aiSpeciesName;
    }

    public Double getAiScore() {
        return aiScore;
    }

    public void setAiScore(Double aiScore) {
        this.aiScore = aiScore;
    }

    public String getAiType() {
        return aiType;
    }

    public void setAiType(String aiType) {
        this.aiType = aiType;
    }

    public String getBaikeUrl() {
        return baikeUrl;
    }

    public void setBaikeUrl(String baikeUrl) {
        this.baikeUrl = baikeUrl;
    }

    public String getBaikeImageUrl() {
        return baikeImageUrl;
    }

    public void setBaikeImageUrl(String baikeImageUrl) {
        this.baikeImageUrl = baikeImageUrl;
    }

    public String getBaikeDescription() {
        return baikeDescription;
    }

    public void setBaikeDescription(String baikeDescription) {
        this.baikeDescription = baikeDescription;
    }

    public Integer getVoteAgreeScore() {
        return voteAgreeScore;
    }

    public void setVoteAgreeScore(Integer voteAgreeScore) {
        this.voteAgreeScore = voteAgreeScore;
    }

    public Integer getVoteDisagreeScore() {
        return voteDisagreeScore;
    }

    public void setVoteDisagreeScore(Integer voteDisagreeScore) {
        this.voteDisagreeScore = voteDisagreeScore;
    }

    public String getVoteStatus() {
        return voteStatus;
    }

    public void setVoteStatus(String voteStatus) {
        this.voteStatus = voteStatus;
    }

    public String getVoteResult() {
        return voteResult;
    }

    public void setVoteResult(String voteResult) {
        this.voteResult = voteResult;
    }
    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public void setIdentificationId(Long identificationId) 
    {
        this.identificationId = identificationId;
    }

    public Long getIdentificationId() 
    {
        return identificationId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public String getUserName() 
    {
        return userName;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setImages(String images) 
    {
        this.images = images;
    }

    public String getImages() 
    {
        return images;
    }

    public void setObservationTime(Date observationTime) 
    {
        this.observationTime = observationTime;
    }

    public Date getObservationTime() 
    {
        return observationTime;
    }

    public void setLocation(String location) 
    {
        this.location = location;
    }

    public String getLocation() 
    {
        return location;
    }

    public void setLatitude(BigDecimal latitude) 
    {
        this.latitude = latitude;
    }

    public BigDecimal getLatitude() 
    {
        return latitude;
    }

    public void setLongitude(BigDecimal longitude) 
    {
        this.longitude = longitude;
    }

    public BigDecimal getLongitude() 
    {
        return longitude;
    }

    public void setHabitat(String habitat) 
    {
        this.habitat = habitat;
    }

    public String getHabitat() 
    {
        return habitat;
    }

    public void setFeatures(String features) 
    {
        this.features = features;
    }

    public String getFeatures() 
    {
        return features;
    }

    public void setStatus(Integer status) 
    {
        this.status = status;
    }

    public Integer getStatus() 
    {
        return status;
    }

    public void setBestAnswerId(Long bestAnswerId) 
    {
        this.bestAnswerId = bestAnswerId;
    }

    public Long getBestAnswerId() 
    {
        return bestAnswerId;
    }

    public void setAnswerCount(Integer answerCount) 
    {
        this.answerCount = answerCount;
    }

    public Integer getAnswerCount() 
    {
        return answerCount;
    }

    public void setViewCount(Integer viewCount) 
    {
        this.viewCount = viewCount;
    }

    public Integer getViewCount() 
    {
        return viewCount;
    }

    public void setIsShared(Integer isShared) 
    {
        this.isShared = isShared;
    }

    public Integer getIsShared() 
    {
        return isShared;
    }

    public void setSharedTopicId(Long sharedTopicId) 
    {
        this.sharedTopicId = sharedTopicId;
    }

    public Long getSharedTopicId() 
    {
        return sharedTopicId;
    }

    public void setAuditStatus(Integer auditStatus) 
    {
        this.auditStatus = auditStatus;
    }

    public Integer getAuditStatus() 
    {
        return auditStatus;
    }

    public void setAuditTime(Date auditTime) 
    {
        this.auditTime = auditTime;
    }

    public Date getAuditTime() 
    {
        return auditTime;
    }

    public void setAuditBy(String auditBy) 
    {
        this.auditBy = auditBy;
    }

    public String getAuditBy() 
    {
        return auditBy;
    }

    public void setAuditRemark(String auditRemark) 
    {
        this.auditRemark = auditRemark;
    }

    public String getAuditRemark() 
    {
        return auditRemark;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("identificationId", getIdentificationId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("images", getImages())
            .append("observationTime", getObservationTime())
            .append("location", getLocation())
            .append("latitude", getLatitude())
            .append("longitude", getLongitude())
            .append("habitat", getHabitat())
            .append("features", getFeatures())
            .append("status", getStatus())
            .append("bestAnswerId", getBestAnswerId())
            .append("answerCount", getAnswerCount())
            .append("viewCount", getViewCount())
            .append("isShared", getIsShared())
            .append("sharedTopicId", getSharedTopicId())
            .append("auditStatus", getAuditStatus())
            .append("auditTime", getAuditTime())
            .append("auditBy", getAuditBy())
            .append("auditRemark", getAuditRemark())
            .append("delFlag", getDelFlag())
            .append("aiSpeciesName", getAiSpeciesName())
            .append("aiScore", getAiScore())
            .append("aiType", getAiType())
            .append("baikeUrl", getBaikeUrl())
            .append("baikeImageUrl", getBaikeImageUrl())
            .append("baikeDescription", getBaikeDescription())
            .append("voteAgreeScore", getVoteAgreeScore())
            .append("voteDisagreeScore", getVoteDisagreeScore())
            .append("voteStatus", getVoteStatus())
            .append("voteResult", getVoteResult())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
