package com.naturalhub.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.naturalhub.common.annotation.Excel;
import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 野外调查记录对象 nh_field_survey
 *
 * @author NaturalHub
 * @date 2026-03-09
 */
public class FieldSurvey extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long surveyId;

    @Excel(name = "用户ID")
    private Long userId;

    @Excel(name = "用户名")
    private String userName;

    @Excel(name = "调查标题")
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "调查日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date surveyDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @Excel(name = "调查地点")
    private String location;

    private String routeInfo;

    @Excel(name = "天气情况")
    private String weather;

    @Excel(name = "温度")
    private String temperature;

    /**
     * 生境类型 tinyint: 1-森林 2-草地 3-湿地 4-河流 5-湖泊 6-海洋 7-山地 8-农田 9-城市 10-其他
     * 【字典编码：nh_habitat_type】
     */
    @Excel(name = "生境类型")
    private Integer habitatType;

    /**
     * 调查方法 tinyint: 1-样线法 2-样方法 3-定点观察 4-随机游走 5-陷阱法 6-网捕法 7-声音记录 8-红外相机 9-综合调查 10-其他
     * 【字典编码：nh_survey_method】
     */
    @Excel(name = "调查方法")
    private Integer surveyMethod;

    private String teamMembers;

    @Excel(name = "发现物种数")
    private Integer speciesCount;

    private String speciesList;

    @Excel(name = "调查描述")
    private String description;

    @Excel(name = "主要发现")
    private String findings;

    private String images;
    private String attachments;

    /** 是否已分享到社群 tinyint: 0-否 1-是 【字典编码：nh_yes_no】 */
    @Excel(name = "是否已分享", readConverterExp = "0=否,1=是")
    private Integer isShared;

    private Long sharedTopicId;

    /** 审核状态 tinyint: 0-草稿 1-待审核 2-已通过 3-已驳回 【字典编码：nh_audit_status】 */
    @Excel(name = "审核状态", readConverterExp = "0=草稿,1=待审核,2=已通过,3=已驳回")
    private Integer auditStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;
    private String auditBy;
    private String auditRemark;
    private String delFlag;

    // ===== 字典标签转换方法 =====

    public String getAuditStatusLabel() {
        if (auditStatus == null) return "";
        switch (auditStatus) {
            case 0: return "草稿";
            case 1: return "待审核";
            case 2: return "已通过";
            case 3: return "已驳回";
            default: return "";
        }
    }

    public String getHabitatTypeLabel() {
        if (habitatType == null) return "";
        switch (habitatType) {
            case 1: return "森林"; case 2: return "草地"; case 3: return "湿地";
            case 4: return "河流"; case 5: return "湖泊"; case 6: return "海洋";
            case 7: return "山地"; case 8: return "农田"; case 9: return "城市";
            case 10: return "其他"; default: return "";
        }
    }

    public String getSurveyMethodLabel() {
        if (surveyMethod == null) return "";
        switch (surveyMethod) {
            case 1: return "样线法"; case 2: return "样方法"; case 3: return "定点观察";
            case 4: return "随机游走"; case 5: return "陷阱法"; case 6: return "网捕法";
            case 7: return "声音记录"; case 8: return "红外相机"; case 9: return "综合调查";
            case 10: return "其他"; default: return "";
        }
    }

    public String getIsSharedLabel() {
        if (isShared == null) return "否";
        return isShared == 1 ? "是" : "否";
    }

    // ===== getters / setters =====

    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public Long getSurveyId() { return surveyId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getUserId() { return userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserName() { return userName; }
    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }
    public void setSurveyDate(Date surveyDate) { this.surveyDate = surveyDate; }
    public Date getSurveyDate() { return surveyDate; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getStartTime() { return startTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public Date getEndTime() { return endTime; }
    public void setLocation(String location) { this.location = location; }
    public String getLocation() { return location; }
    public void setRouteInfo(String routeInfo) { this.routeInfo = routeInfo; }
    public String getRouteInfo() { return routeInfo; }
    public void setWeather(String weather) { this.weather = weather; }
    public String getWeather() { return weather; }
    public void setTemperature(String temperature) { this.temperature = temperature; }
    public String getTemperature() { return temperature; }
    /** 【改造点】Integer */
    public void setHabitatType(Integer habitatType) { this.habitatType = habitatType; }
    public Integer getHabitatType() { return habitatType; }
    /** 【改造点】Integer */
    public void setSurveyMethod(Integer surveyMethod) { this.surveyMethod = surveyMethod; }
    public Integer getSurveyMethod() { return surveyMethod; }
    public void setTeamMembers(String teamMembers) { this.teamMembers = teamMembers; }
    public String getTeamMembers() { return teamMembers; }
    public void setSpeciesCount(Integer speciesCount) { this.speciesCount = speciesCount; }
    public Integer getSpeciesCount() { return speciesCount; }
    public void setSpeciesList(String speciesList) { this.speciesList = speciesList; }
    public String getSpeciesList() { return speciesList; }
    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }
    public void setFindings(String findings) { this.findings = findings; }
    public String getFindings() { return findings; }
    public void setImages(String images) { this.images = images; }
    public String getImages() { return images; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public String getAttachments() { return attachments; }
    public void setIsShared(Integer isShared) { this.isShared = isShared; }
    public Integer getIsShared() { return isShared; }
    public void setSharedTopicId(Long sharedTopicId) { this.sharedTopicId = sharedTopicId; }
    public Long getSharedTopicId() { return sharedTopicId; }
    public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }
    public Integer getAuditStatus() { return auditStatus; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public String getAuditBy() { return auditBy; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getAuditRemark() { return auditRemark; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("surveyId", getSurveyId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("title", getTitle())
            .append("surveyDate", getSurveyDate())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("location", getLocation())
            .append("routeInfo", getRouteInfo())
            .append("weather", getWeather())
            .append("temperature", getTemperature())
            .append("habitatType", getHabitatType())
            .append("surveyMethod", getSurveyMethod())
            .append("teamMembers", getTeamMembers())
            .append("speciesCount", getSpeciesCount())
            .append("speciesList", getSpeciesList())
            .append("description", getDescription())
            .append("findings", getFindings())
            .append("images", getImages())
            .append("attachments", getAttachments())
            .append("isShared", getIsShared())
            .append("sharedTopicId", getSharedTopicId())
            .append("auditStatus", getAuditStatus())
            .append("auditTime", getAuditTime())
            .append("auditBy", getAuditBy())
            .append("auditRemark", getAuditRemark())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
