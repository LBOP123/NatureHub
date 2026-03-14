package com.naturalhub.system.mapper;

import java.util.List;
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
    public int setBestAnswer(Long identificationId, Long answerId);

    /**
     * 根据分享话题ID查询物种鉴定记录
     * 
     * @param sharedTopicId 分享话题ID
     * @return 物种鉴定记录
     */
    public SpeciesIdentification selectSpeciesIdentificationBySharedTopicId(Long sharedTopicId);
}
