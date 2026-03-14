package com.naturalhub.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.naturalhub.common.annotation.Excel;
import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 鉴定回答对象 nh_identification_answer
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
public class IdentificationAnswer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 回答ID */
    private Long answerId;

    /** 鉴定ID */
    @Excel(name = "鉴定ID")
    private Long identificationId;

    /** 回答用户ID */
    @Excel(name = "回答用户ID")
    private Long userId;

    /** 回答用户名 */
    @Excel(name = "回答用户名")
    private String userName;

    /** 回答内容 */
    @Excel(name = "回答内容")
    private String content;

    /** 物种名称 */
    @Excel(name = "物种名称")
    private String speciesName;

    /** 置信度 */
    @Excel(name = "置信度", readConverterExp = "high=高,medium=中,low=低")
    private String confidence;

    /** 参考资料 */
    @Excel(name = "参考资料")
    private String reference;

    /** 参考图片 */
    private String images;

    /** 是否最佳答案【字典编码：nh_yes_no】0=否,1=是 */
    @Excel(name = "是否最佳答案", readConverterExp = "0=否,1=是")
    private Integer isBest;

    /** 点赞数 */
    @Excel(name = "点赞数")
    private Integer likeCount;

    /** 删除标志 */
    private String delFlag;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public void setAnswerId(Long answerId) 
    {
        this.answerId = answerId;
    }

    public Long getAnswerId() 
    {
        return answerId;
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

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setSpeciesName(String speciesName) 
    {
        this.speciesName = speciesName;
    }

    public String getSpeciesName() 
    {
        return speciesName;
    }

    public void setConfidence(String confidence) 
    {
        this.confidence = confidence;
    }

    public String getConfidence() 
    {
        return confidence;
    }

    public void setReference(String reference) 
    {
        this.reference = reference;
    }

    public String getReference() 
    {
        return reference;
    }

    public void setImages(String images) 
    {
        this.images = images;
    }

    public String getImages() 
    {
        return images;
    }

    public void setIsBest(Integer isBest) 
    {
        this.isBest = isBest;
    }

    public Integer getIsBest() 
    {
        return isBest;
    }

    public void setLikeCount(Integer likeCount) 
    {
        this.likeCount = likeCount;
    }

    public Integer getLikeCount() 
    {
        return likeCount;
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
    public Date getCreateTime() 
    {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) 
    {
        this.createTime = createTime;
    }

    @Override
    public Date getUpdateTime() 
    {
        return updateTime;
    }

    @Override
    public void setUpdateTime(Date updateTime) 
    {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("answerId", getAnswerId())
            .append("identificationId", getIdentificationId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("content", getContent())
            .append("speciesName", getSpeciesName())
            .append("confidence", getConfidence())
            .append("reference", getReference())
            .append("images", getImages())
            .append("isBest", getIsBest())
            .append("likeCount", getLikeCount())
            .append("delFlag", getDelFlag())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
