package com.naturalhub.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.naturalhub.common.annotation.Excel;
import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 社群举报对象 nh_community_report
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public class CommunityReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 举报ID */
    private Long reportId;

    /** 举报类型 */
    @Excel(name = "举报类型", readConverterExp = "topic=话题,comment=评论")
    private String reportType;

    /** 被举报对象ID */
    @Excel(name = "被举报对象ID")
    private Long targetId;

    /** 被举报对象标题/内容摘要 */
    @Excel(name = "被举报内容")
    private String targetTitle;

    /** 被举报用户ID */
    private Long targetUserId;

    /** 被举报用户名 */
    @Excel(name = "被举报用户")
    private String targetUserName;

    /** 举报人ID */
    private Long reporterId;

    /** 举报人名称 */
    @Excel(name = "举报人")
    private String reporterName;

    /** 举报原因 */
    @Excel(name = "举报原因", readConverterExp = "spam=垃圾广告,abuse=辱骂攻击,illegal=违法违规,false=虚假信息,other=其他")
    private String reason;

    /** 举报详细描述 */
    @Excel(name = "举报描述")
    private String description;

    /** 处理状态 */
    @Excel(name = "处理状态", readConverterExp = "pending=待处理,processed=已处理,ignored=已忽略")
    private String status;

    /** 处理结果 */
    @Excel(name = "处理结果", readConverterExp = "delete_content=删除内容,ban_user=禁言用户,warning=警告,ignore=忽略")
    private String handleResult;

    /** 处理备注 */
    @Excel(name = "处理备注")
    private String handleRemark;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "处理时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 处理人 */
    @Excel(name = "处理人")
    private String handleBy;

    public void setReportId(Long reportId) 
    {
        this.reportId = reportId;
    }

    public Long getReportId() 
    {
        return reportId;
    }

    public void setReportType(String reportType) 
    {
        this.reportType = reportType;
    }

    public String getReportType() 
    {
        return reportType;
    }

    public void setTargetId(Long targetId) 
    {
        this.targetId = targetId;
    }

    public Long getTargetId() 
    {
        return targetId;
    }

    public void setTargetTitle(String targetTitle) 
    {
        this.targetTitle = targetTitle;
    }

    public String getTargetTitle() 
    {
        return targetTitle;
    }

    public void setTargetUserId(Long targetUserId) 
    {
        this.targetUserId = targetUserId;
    }

    public Long getTargetUserId() 
    {
        return targetUserId;
    }

    public void setTargetUserName(String targetUserName) 
    {
        this.targetUserName = targetUserName;
    }

    public String getTargetUserName() 
    {
        return targetUserName;
    }

    public void setReporterId(Long reporterId) 
    {
        this.reporterId = reporterId;
    }

    public Long getReporterId() 
    {
        return reporterId;
    }

    public void setReporterName(String reporterName) 
    {
        this.reporterName = reporterName;
    }

    public String getReporterName() 
    {
        return reporterName;
    }

    public void setReason(String reason) 
    {
        this.reason = reason;
    }

    public String getReason() 
    {
        return reason;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setHandleResult(String handleResult) 
    {
        this.handleResult = handleResult;
    }

    public String getHandleResult() 
    {
        return handleResult;
    }

    public void setHandleRemark(String handleRemark) 
    {
        this.handleRemark = handleRemark;
    }

    public String getHandleRemark() 
    {
        return handleRemark;
    }

    public void setHandleTime(Date handleTime) 
    {
        this.handleTime = handleTime;
    }

    public Date getHandleTime() 
    {
        return handleTime;
    }

    public void setHandleBy(String handleBy) 
    {
        this.handleBy = handleBy;
    }

    public String getHandleBy() 
    {
        return handleBy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("reportId", getReportId())
            .append("reportType", getReportType())
            .append("targetId", getTargetId())
            .append("targetTitle", getTargetTitle())
            .append("targetUserId", getTargetUserId())
            .append("targetUserName", getTargetUserName())
            .append("reporterId", getReporterId())
            .append("reporterName", getReporterName())
            .append("reason", getReason())
            .append("description", getDescription())
            .append("status", getStatus())
            .append("handleResult", getHandleResult())
            .append("handleRemark", getHandleRemark())
            .append("handleTime", getHandleTime())
            .append("handleBy", getHandleBy())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
