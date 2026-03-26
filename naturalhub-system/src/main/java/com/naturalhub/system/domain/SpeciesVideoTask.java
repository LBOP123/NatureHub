package com.naturalhub.system.domain;

import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 物种科普视频生成任务
 *
 * @author NaturalHub
 */
public class SpeciesVideoTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String username;
    private String content;
    private String taskStatus;
    private String videoUrl;
    private String errorMessage;
    private Long conversationId;
    /** 关联的 qa_history ID（生成完成/失败后回填 answer） */
    private Long qaHistoryId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getQaHistoryId() { return qaHistoryId; }
    public void setQaHistoryId(Long qaHistoryId) { this.qaHistoryId = qaHistoryId; }
}
