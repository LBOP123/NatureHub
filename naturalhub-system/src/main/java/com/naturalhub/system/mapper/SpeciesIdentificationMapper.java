package com.naturalhub.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.naturalhub.system.domain.SpeciesIdentification;

/**
 * 物种鉴定求助Mapper接口
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
public interface SpeciesIdentificationMapper 
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
     * 删除物种鉴定求助
     * 
     * @param identificationId 物种鉴定求助主键
     * @return 结果
     */
    public int deleteSpeciesIdentificationByIdentificationId(Long identificationId);

    /**
     * 批量删除物种鉴定求助
     * 
     * @param identificationIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSpeciesIdentificationByIdentificationIds(Long[] identificationIds);

    /**
     * 增加浏览次数
     * 
     * @param identificationId 鉴定ID
     * @return 结果
     */
    public int incrementViewCount(Long identificationId);

    /**
     * 更新回答数量
     * 
     * @param identificationId 鉴定ID
     * @return 结果
     */
    public int updateAnswerCount(Long identificationId);

    /**
     * 设置最佳答案
     * 
     * @param identificationId 鉴定ID
     * @param answerId 答案ID
     * @return 结果
     */
    public int setBestAnswer(@Param("identificationId") Long identificationId, @Param("answerId") Long answerId);

    /**
     * 根据分享话题ID查询物种鉴定记录
     *
     * @param sharedTopicId 分享话题ID
     * @return 物种鉴定记录
     */
    public SpeciesIdentification selectSpeciesIdentificationBySharedTopicId(Long sharedTopicId);

    /**
     * 更新投票积分和结果
     *
     * @param identificationId 鉴定ID
     * @param voteStatus 投票状态
     * @param voteResult 投票结果
     * @param agreeScore 同意积分
     * @param disagreeScore 不同意积分
     * @return 结果
     */
    public int updateVoteResult(@Param("identificationId") Long identificationId, @Param("voteStatus") String voteStatus, @Param("voteResult") String voteResult, @Param("agreeScore") Integer agreeScore, @Param("disagreeScore") Integer disagreeScore);

    /**
     * 更新鉴定状态
     *
     * @param identificationId 鉴定ID
     * @param status 状态
     * @return 结果
     */
    public int updateStatus(@Param("identificationId") Long identificationId, @Param("status") Integer status);
}
