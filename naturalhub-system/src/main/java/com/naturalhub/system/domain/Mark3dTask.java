package com.naturalhub.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 3D标注任务实体
 *
 * @author NaturalHub
 */
public class Mark3dTask {

    private Long id;
    private Long userId;
    private String username;
    private String taskName;

    /** 输入图片URL（七牛云） */
    private String frontImageUrl;
    /** 背面图片URL（预留） */
    private String backImageUrl;
    /** 上面图片URL（预留） */
    private String topImageUrl;
    /** 侧面图片URL（预留） */
    private String sideImageUrl;

    /** 腾讯云混元3D JobId */
    private String meshyTaskId;

    /**
     * 任务状态 char(1)，对应字典 mark3d_task_status
     * 1-等待中  2-生成中  3-成功  4-失败
     */
    private String taskStatus;

    /** 生成进度 0-100 */
    private Integer progress;

    /** GLB 模型地址（腾讯云临时，24小时有效）*/
    private String modelUrlGlb;
    /** GLB 模型地址（七牛云持久化，永久有效）*/
    private String modelUrlGlbQiniu;
    /** OBJ 模型地址（腾讯云临时）*/
    private String modelUrlObj;
    /** USDZ 模型地址 */
    private String modelUrlUsdz;
    /** FBX 模型地址 */
    private String modelUrlFbx;
    /** 预览图URL（腾讯云临时）*/
    private String thumbnailUrl;
    /** 预览图URL（七牛云持久化，永久有效）*/
    private String thumbnailQiniu;

    /** 失败错误信息 */
    private String errorMessage;
    private String remark;

    /** 是否公开展示（0-不公开 1-公开）*/
    private Integer isPublic;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // ===== getters / setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getFrontImageUrl() { return frontImageUrl; }
    public void setFrontImageUrl(String frontImageUrl) { this.frontImageUrl = frontImageUrl; }

    public String getBackImageUrl() { return backImageUrl; }
    public void setBackImageUrl(String backImageUrl) { this.backImageUrl = backImageUrl; }

    public String getTopImageUrl() { return topImageUrl; }
    public void setTopImageUrl(String topImageUrl) { this.topImageUrl = topImageUrl; }

    public String getSideImageUrl() { return sideImageUrl; }
    public void setSideImageUrl(String sideImageUrl) { this.sideImageUrl = sideImageUrl; }

    public String getMeshyTaskId() { return meshyTaskId; }
    public void setMeshyTaskId(String meshyTaskId) { this.meshyTaskId = meshyTaskId; }

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getModelUrlGlb() { return modelUrlGlb; }
    public void setModelUrlGlb(String modelUrlGlb) { this.modelUrlGlb = modelUrlGlb; }

    public String getModelUrlGlbQiniu() { return modelUrlGlbQiniu; }
    public void setModelUrlGlbQiniu(String modelUrlGlbQiniu) { this.modelUrlGlbQiniu = modelUrlGlbQiniu; }

    public String getModelUrlObj() { return modelUrlObj; }
    public void setModelUrlObj(String modelUrlObj) { this.modelUrlObj = modelUrlObj; }

    public String getModelUrlUsdz() { return modelUrlUsdz; }
    public void setModelUrlUsdz(String modelUrlUsdz) { this.modelUrlUsdz = modelUrlUsdz; }

    public String getModelUrlFbx() { return modelUrlFbx; }
    public void setModelUrlFbx(String modelUrlFbx) { this.modelUrlFbx = modelUrlFbx; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getThumbnailQiniu() { return thumbnailQiniu; }
    public void setThumbnailQiniu(String thumbnailQiniu) { this.thumbnailQiniu = thumbnailQiniu; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getIsPublic() { return isPublic; }
    public void setIsPublic(Integer isPublic) { this.isPublic = isPublic; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
