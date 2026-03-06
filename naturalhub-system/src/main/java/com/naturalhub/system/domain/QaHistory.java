package com.naturalhub.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * AI问答历史记录
 */
public class QaHistory {
    
    private Long id;
    private Long userId;
    private Long conversationId;
    private String username;
    private String question;
    private String answer;
    private Integer qaType;
    private String relatedSpecies;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    private String ipAddress;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
