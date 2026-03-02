package com.naturalhub.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.naturalhub.common.annotation.Excel;
import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 个人观察日志对象 nh_observation_diary
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public class ObservationDiary extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long diaryId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 日志标题 */
    @Excel(name = "日志标题")
    private String title;

    /** 观察日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "观察日期", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date observationDate;

    /** 观察地点 */
    @Excel(name = "观察地点")
    private String location;

    /** 路线信息 */
    @Excel(name = "路线信息")
    private String routeInfo;

    /** 天气情况 */
    @Excel(name = "天气情况")
    private String weather;

    /** 温度 */
    @Excel(name = "温度")
    private String temperature;

    /** 发现物种（JSON数组） */
    @Excel(name = "发现物种")
    private String speciesFound;

    /** 日志内容/心得体会 */
    @Excel(name = "日志内容")
    private String content;

    /** 图片路径（JSON数组） */
    private String images;

    /** 可见性（0私密 1公开） */
    @Excel(name = "可见性", readConverterExp = "0=私密,1=公开")
    private String visibility;

    /** 是否归档（0否 1是） */
    @Excel(name = "是否归档", readConverterExp = "0=否,1=是")
    private String isArchived;

    /** 标签 */
    @Excel(name = "标签")
    private String tags;

    /** 关联观察记录ID（逗号分隔） */
    private String observationIds;

    public void setDiaryId(Long diaryId) 
    {
        this.diaryId = diaryId;
    }

    public Long getDiaryId() 
    {
        return diaryId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setObservationDate(Date observationDate) 
    {
        this.observationDate = observationDate;
    }

    public Date getObservationDate() 
    {
        return observationDate;
    }

    public void setLocation(String location) 
    {
        this.location = location;
    }

    public String getLocation() 
    {
        return location;
    }

    public void setRouteInfo(String routeInfo) 
    {
        this.routeInfo = routeInfo;
    }

    public String getRouteInfo() 
    {
        return routeInfo;
    }

    public void setWeather(String weather) 
    {
        this.weather = weather;
    }

    public String getWeather() 
    {
        return weather;
    }

    public void setTemperature(String temperature) 
    {
        this.temperature = temperature;
    }

    public String getTemperature() 
    {
        return temperature;
    }

    public void setSpeciesFound(String speciesFound) 
    {
        this.speciesFound = speciesFound;
    }

    public String getSpeciesFound() 
    {
        return speciesFound;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setImages(String images) 
    {
        this.images = images;
    }

    public String getImages() 
    {
        return images;
    }

    public void setVisibility(String visibility) 
    {
        this.visibility = visibility;
    }

    public String getVisibility() 
    {
        return visibility;
    }

    public void setIsArchived(String isArchived) 
    {
        this.isArchived = isArchived;
    }

    public String getIsArchived() 
    {
        return isArchived;
    }

    public void setTags(String tags) 
    {
        this.tags = tags;
    }

    public String getTags() 
    {
        return tags;
    }

    public void setObservationIds(String observationIds) 
    {
        this.observationIds = observationIds;
    }

    public String getObservationIds() 
    {
        return observationIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("diaryId", getDiaryId())
            .append("userId", getUserId())
            .append("title", getTitle())
            .append("observationDate", getObservationDate())
            .append("location", getLocation())
            .append("routeInfo", getRouteInfo())
            .append("weather", getWeather())
            .append("temperature", getTemperature())
            .append("speciesFound", getSpeciesFound())
            .append("content", getContent())
            .append("images", getImages())
            .append("visibility", getVisibility())
            .append("isArchived", getIsArchived())
            .append("tags", getTags())
            .append("observationIds", getObservationIds())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
