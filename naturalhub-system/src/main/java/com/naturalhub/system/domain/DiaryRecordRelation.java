package com.naturalhub.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 日志关联观察记录对象 nh_diary_record_relation
 * 
 * @author naturalhub
 * @date 2025-03-21
 */
public class DiaryRecordRelation {
    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private Long id;

    /** 日志ID */
    private Long diaryId;

    /** 观察记录ID */
    private Long recordId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDiaryId(Long diaryId) {
        this.diaryId = diaryId;
    }

    public Long getDiaryId() {
        return diaryId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("diaryId", getDiaryId())
            .append("recordId", getRecordId())
            .append("createTime", getCreateTime())
            .toString();
    }
}
