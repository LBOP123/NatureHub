package com.naturalhub.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.naturalhub.common.annotation.Excel;
import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 观察记录对象 nh_observation_record
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
public class ObservationRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 观察记录ID */
    private Long recordId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 标题 */
    @Excel(name = "标题")
    private String title;

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

    /** 物种类型 0=植物,1=动物,2=真菌,3=其他 */
    @Excel(name = "物种类型")
    private Integer speciesType;

    /** 物种名称 */
    @Excel(name = "物种名称")
    private String speciesName;

    /** 生境描述 */
    @Excel(name = "生境描述")
    private String habitat;

    /** 详细描述 */
    @Excel(name = "详细描述")
    private String description;

    /** 图片URL列表 */
    private String images;

    /** 视频URL列表 */
    private String videos;

    /** 审核状态 0=草稿,1=待审核,2=已通过,3=已驳回 */
    @Excel(name = "审核状态")
    private Integer auditStatus;

    /** 提交审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;

    /** 审核人ID */
    private Long reviewerId;

    /** 审核意见 */
    private String reviewComment;

    /** 驳回原因 */
    private String rejectReason;

    /** 关联的标本ID */
    private Long specimenId;

    /** 是否已分享到社群 0=否,1=是 */
    private Integer isShared;

    /** 分享的社群话题ID */
    private Long sharedTopicId;

    /** 删除标志 */
    private String delFlag;

    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getRecordId() { return recordId; }

    public void setUserId(Long userId) { this.userId = userId; }
    public Long getUserId() { return userId; }

    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    public void setObservationTime(Date observationTime) { this.observationTime = observationTime; }
    public Date getObservationTime() { return observationTime; }

    public void setLocation(String location) { this.location = location; }
    public String getLocation() { return location; }

    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLatitude() { return latitude; }

    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLongitude() { return longitude; }

    public void setSpeciesType(Integer speciesType) { this.speciesType = speciesType; }
    public Integer getSpeciesType() { return speciesType; }

    public void setSpeciesName(String speciesName) { this.speciesName = speciesName; }
    public String getSpeciesName() { return speciesName; }

    public void setHabitat(String habitat) { this.habitat = habitat; }
    public String getHabitat() { return habitat; }

    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }

    public void setImages(String images) { this.images = images; }
    public String getImages() { return images; }

    public void setVideos(String videos) { this.videos = videos; }
    public String getVideos() { return videos; }

    public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }
    public Integer getAuditStatus() { return auditStatus; }

    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    public Date getSubmitTime() { return submitTime; }

    public void setReviewTime(Date reviewTime) { this.reviewTime = reviewTime; }
    public Date getReviewTime() { return reviewTime; }

    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public Long getReviewerId() { return reviewerId; }

    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public String getReviewComment() { return reviewComment; }

    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getRejectReason() { return rejectReason; }

    public void setSpecimenId(Long specimenId) { this.specimenId = specimenId; }
    public Long getSpecimenId() { return specimenId; }

    public void setIsShared(Integer isShared) { this.isShared = isShared; }
    public Integer getIsShared() { return isShared; }

    public void setSharedTopicId(Long sharedTopicId) { this.sharedTopicId = sharedTopicId; }
    public Long getSharedTopicId() { return sharedTopicId; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("userId", getUserId())
            .append("title", getTitle())
            .append("observationTime", getObservationTime())
            .append("location", getLocation())
            .append("latitude", getLatitude())
            .append("longitude", getLongitude())
            .append("speciesType", getSpeciesType())
            .append("speciesName", getSpeciesName())
            .append("habitat", getHabitat())
            .append("description", getDescription())
            .append("images", getImages())
            .append("videos", getVideos())
            .append("auditStatus", getAuditStatus())
            .append("submitTime", getSubmitTime())
            .append("reviewTime", getReviewTime())
            .append("reviewerId", getReviewerId())
            .append("reviewComment", getReviewComment())
            .append("rejectReason", getRejectReason())
            .append("specimenId", getSpecimenId())
            .append("isShared", getIsShared())
            .append("sharedTopicId", getSharedTopicId())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
