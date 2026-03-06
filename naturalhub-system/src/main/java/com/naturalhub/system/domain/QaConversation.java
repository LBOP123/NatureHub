package com.naturalhub.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 对话会话
 */
public class QaConversation {
    
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private Integer qaType; // 1-普通问答，2-知识图谱问答
    private String relatedSpecies;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

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

    public Integer getQaType() {
        return qaType;
    }

    public void setQaType(Integer qaType) {
        this.qaType = qaType;
    }

    public String getRelatedSpecies() {
        return relatedSpecies;
    }

    public void setRelatedSpecies(String relatedSpecies) {
        this.relatedSpecies = relatedSpecies;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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
