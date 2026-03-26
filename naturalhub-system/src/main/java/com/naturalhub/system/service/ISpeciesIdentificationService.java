package com.naturalhub.system.service;

import java.util.List;
import com.naturalhub.system.domain.SpeciesIdentification;

/**
 * 物种鉴定求助Service接口
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
public interface ISpeciesIdentificationService 
{
    /**
     * 查询物种鉴定求助
     * 
     * @param identificationId 物种鉴定求助主键
     * @return 物种鉴定求助
     */
    public SpeciesIdentification selectSpeciesIdentificationByIdentificationId(Long identificationId);

    /**
     * 查询物种鉴定求助列表
     * 
     * @param speciesIdentification 物种鉴定求助
     * @return 物种鉴定求助集合
     */
    public List<SpeciesIdentification> selectSpeciesIdentificationList(SpeciesIdentification speciesIdentification);

    /**
     * 新增物种鉴定求助
     * 
     * @param speciesIdentification 物种鉴定求助
     * @return 结果
     */
    public int insertSpeciesIdentification(SpeciesIdentification speciesIdentification);

    /**
     * 修改物种鉴定求助
     * 
     * @param speciesIdentification 物种鉴定求助
     * @return 结果
     */
    public int updateSpeciesIdentification(SpeciesIdentification speciesIdentification);

    /**
     * 批量删除物种鉴定求助
     * 
     * @param identificationIds 需要删除的物种鉴定求助主键集合
     * @return 结果
     */
    public int deleteSpeciesIdentificationByIdentificationIds(Long[] identificationIds);

    /**
     * 删除物种鉴定求助信息
     * 
     * @param identificationId 物种鉴定求助主键
     * @return 结果
     */
    public int deleteSpeciesIdentificationByIdentificationId(Long identificationId);

    /**
     * 审核物种鉴定求助
     * 
     * @param identificationId 鉴定ID
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     * @return 结果
     */
    public int audit(Long identificationId, Integer auditStatus, String auditRemark);

    /**
     * 增加浏览次数
     * 
     * @param identificationId 鉴定ID
     * @return 结果
     */
    public int incrementViewCount(Long identificationId);

    /**
     * 根据分享话题ID查询物种鉴定记录（兼容历史数据）
     * @param sharedTopicId 话题ID
     * @return 物种鉴定记录
     */
    public SpeciesIdentification selectSpeciesIdentificationBySharedTopicId(Long sharedTopicId);

    /**
     * 设置最佳答案
     * 
     * @param identificationId 鉴定ID
     * @param answerId 答案ID
     * @return 结果
     */
    public int setBestAnswer(Long identificationId, Long answerId);

    /**
     * 分享到社群
     *
     * @param identificationId 鉴定ID
     * @param content          用户填写的分享内容
     * @param createBy         当前操作用户名
     * @return 社群话题ID
     */
    public Long shareToCommunity(Long identificationId, String content, String createBy);

    /**
     * 异步AI识别（分享到社群时触发）
     *
     * @param identificationId 鉴定ID
     */
    public void asyncRecognizeAi(Long identificationId);

    /**
     * 提交投票
     *
     * @param identificationId 鉴定ID
     * @param userId 用户ID
     * @param userName 用户名
     * @param voteType 投票类型：0=同意,1=不同意
     * @return 结果
     */
    public int submitVote(Long identificationId, Long userId, String userName, String voteType);

    /**
     * 获取投票状态信息
     *
     * @param identificationId 鉴定ID
     * @return 投票状态对象
     */
    public VoteStatusInfo getVoteStatus(Long identificationId);

    /**
     * 结束投票
     *
     * @param identificationId 鉴定ID
     * @return 结果
     */
    public int endVoting(Long identificationId);

    /**
     * 获取投票详情列表
     *
     * @param identificationId 鉴定ID
     * @return 投票记录列表
     */
    public java.util.List<com.naturalhub.system.domain.IdentificationVote> getVoteDetails(Long identificationId);

    /**
     * 获取投票数量
     *
     * @param identificationId 鉴定ID
     * @return 投票数量
     */
    public int getVoteCount(Long identificationId);

    /**
     * 投票状态信息类
     */
    public static class VoteStatusInfo {
        /** 同意积分 */
        private int agreeScore;
        /** 不同意积分 */
        private int disagreeScore;
        /** 投票状态 */
        private String voteStatus;
        /** 投票结果：0=同意,1=不同意,2=未确定 */
        private String voteResult;

        public VoteStatusInfo(int agreeScore, int disagreeScore, String voteStatus, String voteResult) {
            this.agreeScore = agreeScore;
            this.disagreeScore = disagreeScore;
            this.voteStatus = voteStatus;
            this.voteResult = voteResult;
        }

        public int getAgreeScore() {
            return agreeScore;
        }

        public void setAgreeScore(int agreeScore) {
            this.agreeScore = agreeScore;
        }

        public int getDisagreeScore() {
            return disagreeScore;
        }

        public void setDisagreeScore(int disagreeScore) {
            this.disagreeScore = disagreeScore;
        }

        public String getVoteStatus() {
            return voteStatus;
        }

        public void setVoteStatus(String voteStatus) {
            this.voteStatus = voteStatus;
        }

        public String getVoteResult() {
            return voteResult;
        }

        public void setVoteResult(String voteResult) {
            this.voteResult = voteResult;
        }
    }
}
