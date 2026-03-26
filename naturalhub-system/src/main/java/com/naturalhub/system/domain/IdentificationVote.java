package com.naturalhub.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.naturalhub.common.annotation.Excel;
import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 物种鉴定投票记录对象 nh_identification_vote
 *
 * @author NaturalHub
 * @date 2026-03-21
 */
public class IdentificationVote extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 投票ID */
    @Excel(name = "投票ID")
    private Long voteId;

    /** 鉴定请求ID */
    @Excel(name = "鉴定请求ID")
    private Long identificationId;

    /** 投票用户ID */
    @Excel(name = "投票用户ID")
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 用户类型：0=探索者, 1=鉴定者 */
    @Excel(name = "用户类型", readConverterExp = "0=探索者,1=鉴定者")
    private String userType;

    /** 投票类型：0=同意, 1=不同意 */
    @Excel(name = "投票类型", readConverterExp = "0=同意,1=不同意")
    private String voteType;

    /** 票权重 */
    @Excel(name = "票权重")
    private Integer voteWeight;

    /** 投票时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "投票时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date voteTime;

    /** 删除标志 */
    private String delFlag;

    // getter and setter
    public void setVoteId(Long voteId)
    {
        this.voteId = voteId;
    }

    public Long getVoteId()
    {
        return voteId;
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

    public void setUserType(String userType)
    {
        this.userType = userType;
    }

    public String getUserType()
    {
        return userType;
    }

    public void setVoteType(String voteType)
    {
        this.voteType = voteType;
    }

    public String getVoteType()
    {
        return voteType;
    }

    public void setVoteWeight(Integer voteWeight)
    {
        this.voteWeight = voteWeight;
    }

    public Integer getVoteWeight()
    {
        return voteWeight;
    }

    public void setVoteTime(Date voteTime)
    {
        this.voteTime = voteTime;
    }

    public Date getVoteTime()
    {
        return voteTime;
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
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("voteId", getVoteId())
            .append("identificationId", getIdentificationId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("userType", getUserType())
            .append("voteType", getVoteType())
            .append("voteWeight", getVoteWeight())
            .append("voteTime", getVoteTime())
            .append("delFlag", getDelFlag())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
