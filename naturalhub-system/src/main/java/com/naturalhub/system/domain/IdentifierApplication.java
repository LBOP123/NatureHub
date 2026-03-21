package com.naturalhub.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.naturalhub.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 鉴定者申请实体
 *
 * @author NaturalHub
 */
public class IdentifierApplication extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 申请ID */
    private Long id;

    /** 申请用户ID */
    private Long userId;

    /** 申请用户名 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 真实姓名 */
    private String realName;

    /** 专业背景/擅长领域 */
    private String expertise;

    /** 个人简介 */
    private String bio;

    /** 资质证明图片URL（逗号分隔） */
    private String qualification;

    /** 鉴定经验描述 */
    private String experience;

    /**
     * 申请状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer status;

    /** 拒绝原因 */
    private String rejectReason;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;

    /** 审核人 */
    private String reviewBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public Date getReviewTime() { return reviewTime; }
    public void setReviewTime(Date reviewTime) { this.reviewTime = reviewTime; }

    public String getReviewBy() { return reviewBy; }
    public void setReviewBy(String reviewBy) { this.reviewBy = reviewBy; }
}
