package com.naturalhub.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 话题评论对象 nh_community_comment
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public class CommunityComment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评论ID */
    private Long commentId;

    /** 话题ID */
    private Long topicId;

    /** 评论用户ID */
    private Long userId;

    /** 评论用户名 */
    private String userName;

    /** 父评论ID */
    private Long parentId;

    /** 回复的用户ID */
    private Long replyToId;

    /** 回复的用户名 */
    private String replyToName;

    /** 评论内容 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 状态 */
    private String status;

    /** 当前用户是否点赞 */
    private Boolean isLiked;

    public void setCommentId(Long commentId) 
    {
        this.commentId = commentId;
    }

    public Long getCommentId() 
    {
        return commentId;
    }

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

    public void setParentId(Long parentId) 
    {
        this.parentId = parentId;
    }

    public Long getParentId() 
    {
        return parentId;
    }

    public void setReplyToId(Long replyToId) 
    {
        this.replyToId = replyToId;
    }

    public Long getReplyToId() 
    {
        return replyToId;
    }

    public void setReplyToName(String replyToName) 
    {
        this.replyToName = replyToName;
    }

    public String getReplyToName() 
    {
        return replyToName;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setLikeCount(Integer likeCount) 
    {
        this.likeCount = likeCount;
    }

    public Integer getLikeCount() 
    {
        return likeCount;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public Boolean getIsLiked() 
    {
        return isLiked;
    }

    public void setIsLiked(Boolean isLiked) 
    {
        this.isLiked = isLiked;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("commentId", getCommentId())
            .append("topicId", getTopicId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("parentId", getParentId())
            .append("replyToId", getReplyToId())
            .append("replyToName", getReplyToName())
            .append("content", getContent())
            .append("likeCount", getLikeCount())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
