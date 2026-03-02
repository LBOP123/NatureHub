package com.naturalhub.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.naturalhub.common.annotation.Excel;
import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 社群话题对象 nh_community_topic
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public class CommunityTopic extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 话题ID */
    private Long topicId;

    /** 发布用户ID */
    @Excel(name = "发布用户ID")
    private Long userId;

    /** 发布用户名 */
    @Excel(name = "发布用户名")
    private String userName;

    /** 话题分类 */
    @Excel(name = "话题分类", readConverterExp = "species_science=物种科普,field_explore=野外探索,identify_help=鉴定求助")
    private String category;

    /** 话题标题 */
    @Excel(name = "话题标题")
    private String title;

    /** 话题内容 */
    @Excel(name = "话题内容")
    private String content;

    /** 图片路径 */
    private String images;

    /** 标签 */
    @Excel(name = "标签")
    private String tags;

    /** 浏览次数 */
    @Excel(name = "浏览次数")
    private Integer viewCount;

    /** 点赞数 */
    @Excel(name = "点赞数")
    private Integer likeCount;

    /** 评论数 */
    @Excel(name = "评论数")
    private Integer commentCount;

    /** 收藏数 */
    @Excel(name = "收藏数")
    private Integer collectCount;

    /** 转发数 */
    @Excel(name = "转发数")
    private Integer shareCount;

    /** 热度分数 */
    @Excel(name = "热度分数")
    private BigDecimal hotScore;

    /** 是否置顶 */
    @Excel(name = "是否置顶", readConverterExp = "0=否,1=是")
    private String isTop;

    /** 是否精华 */
    @Excel(name = "是否精华", readConverterExp = "0=否,1=是")
    private String isEssence;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=关闭")
    private String status;

    /** 审核状态 */
    @Excel(name = "审核状态", readConverterExp = "pending=待审核,approved=已通过,rejected=已驳回")
    private String auditStatus;

    /** 审核备注 */
    private String auditRemark;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核人 */
    private String auditBy;

    /** 当前用户是否点赞 */
    private Boolean isLiked;

    /** 当前用户是否收藏 */
    private Boolean isCollected;

    public void setTopicId(Long topicId) 
    {
        this.topicId = topicId;
    }

    public Long getTopicId() 
    {
        return topicId;
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

    public void setCategory(String category) 
    {
        this.category = category;
    }

    public String getCategory() 
    {
        return category;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
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

    public void setTags(String tags) 
    {
        this.tags = tags;
    }

    public String getTags() 
    {
        return tags;
    }

    public void setViewCount(Integer viewCount) 
    {
        this.viewCount = viewCount;
    }

    public Integer getViewCount() 
    {
        return viewCount;
    }

    public void setLikeCount(Integer likeCount) 
    {
        this.likeCount = likeCount;
    }

    public Integer getLikeCount() 
    {
        return likeCount;
    }

    public void setCommentCount(Integer commentCount) 
    {
        this.commentCount = commentCount;
    }

    public Integer getCommentCount() 
    {
        return commentCount;
    }

    public void setCollectCount(Integer collectCount) 
    {
        this.collectCount = collectCount;
    }

    public Integer getCollectCount() 
    {
        return collectCount;
    }

    public void setShareCount(Integer shareCount) 
    {
        this.shareCount = shareCount;
    }

    public Integer getShareCount() 
    {
        return shareCount;
    }

    public void setHotScore(BigDecimal hotScore) 
    {
        this.hotScore = hotScore;
    }

    public BigDecimal getHotScore() 
    {
        return hotScore;
    }

    public void setIsTop(String isTop) 
    {
        this.isTop = isTop;
    }

    public String getIsTop() 
    {
        return isTop;
    }

    public void setIsEssence(String isEssence) 
    {
        this.isEssence = isEssence;
    }

    public String getIsEssence() 
    {
        return isEssence;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public String getAuditStatus() 
    {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) 
    {
        this.auditStatus = auditStatus;
    }

    public String getAuditRemark() 
    {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) 
    {
        this.auditRemark = auditRemark;
    }

    public Date getAuditTime() 
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime) 
    {
        this.auditTime = auditTime;
    }

    public String getAuditBy() 
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy) 
    {
        this.auditBy = auditBy;
    }

    public Boolean getIsLiked() 
    {
        return isLiked;
    }

    public void setIsLiked(Boolean isLiked) 
    {
        this.isLiked = isLiked;
    }

    public Boolean getIsCollected() 
    {
        return isCollected;
    }

    public void setIsCollected(Boolean isCollected) 
    {
        this.isCollected = isCollected;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("topicId", getTopicId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("category", getCategory())
            .append("title", getTitle())
            .append("content", getContent())
            .append("images", getImages())
            .append("tags", getTags())
            .append("viewCount", getViewCount())
            .append("likeCount", getLikeCount())
            .append("commentCount", getCommentCount())
            .append("collectCount", getCollectCount())
            .append("shareCount", getShareCount())
            .append("hotScore", getHotScore())
            .append("isTop", getIsTop())
            .append("isEssence", getIsEssence())
            .append("status", getStatus())
            .append("auditStatus", getAuditStatus())
            .append("auditRemark", getAuditRemark())
            .append("auditTime", getAuditTime())
            .append("auditBy", getAuditBy())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
