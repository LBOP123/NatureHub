package com.naturalhub.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.common.utils.StringUtils;
import com.naturalhub.system.mapper.SpeciesIdentificationMapper;
import com.naturalhub.system.mapper.IdentificationAnswerMapper;
import com.naturalhub.system.mapper.CommunityTopicMapper;
import com.naturalhub.system.domain.SpeciesIdentification;
import com.naturalhub.system.domain.CommunityTopic;
import com.naturalhub.system.service.ISpeciesIdentificationService;

/**
 * 物种鉴定求助Service业务层处理
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
@Service
public class SpeciesIdentificationServiceImpl implements ISpeciesIdentificationService 
{
    @Autowired
    private SpeciesIdentificationMapper speciesIdentificationMapper;

    @Autowired
    private IdentificationAnswerMapper identificationAnswerMapper;

    @Autowired
    private CommunityTopicMapper communityTopicMapper;

    /**
     * 查询物种鉴定求助
     * 
     * @param identificationId 物种鉴定求助主键
     * @return 物种鉴定求助
     */
    @Override
    public SpeciesIdentification selectSpeciesIdentificationByIdentificationId(Long identificationId)
    {
        return speciesIdentificationMapper.selectSpeciesIdentificationByIdentificationId(identificationId);
    }

    /**
     * 查询物种鉴定求助列表
     * 
     * @param speciesIdentification 物种鉴定求助
     * @return 物种鉴定求助
     */
    @Override
    public List<SpeciesIdentification> selectSpeciesIdentificationList(SpeciesIdentification speciesIdentification)
    {
        return speciesIdentificationMapper.selectSpeciesIdentificationList(speciesIdentification);
    }

    /**
     * 新增物种鉴定求助
     * 
     * @param speciesIdentification 物种鉴定求助
     * @return 结果
     */
    @Override
    public int insertSpeciesIdentification(SpeciesIdentification speciesIdentification)
    {
        speciesIdentification.setStatus(0); // 0=待鉴定
        // 保留前端传入的 auditStatus（0=草稿,1=待审核）
        if (speciesIdentification.getAuditStatus() == null) {
            speciesIdentification.setAuditStatus(0);
        }
        speciesIdentification.setAnswerCount(0);
        speciesIdentification.setViewCount(0);
        speciesIdentification.setIsShared(0); // 0=否
        speciesIdentification.setDelFlag("0");
        speciesIdentification.setCreateTime(new Date());
        return speciesIdentificationMapper.insertSpeciesIdentification(speciesIdentification);
    }

    /**
     * 修改物种鉴定求助
     * 
     * @param speciesIdentification 物种鉴定求助
     * @return 结果
     */
    @Override
    public int updateSpeciesIdentification(SpeciesIdentification speciesIdentification)
    {
        speciesIdentification.setUpdateTime(new Date());
        return speciesIdentificationMapper.updateSpeciesIdentification(speciesIdentification);
    }

    /**
     * 批量删除物种鉴定求助
     * 
     * @param identificationIds 需要删除的物种鉴定求助主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSpeciesIdentificationByIdentificationIds(Long[] identificationIds)
    {
        // 逐个删除，每个都检查并删除关联的社群话题
        for (Long identificationId : identificationIds) {
            // 先查询该记录对应的社群话题
            CommunityTopic topic = communityTopicMapper.selectBySourceTypeAndSourceId(2, identificationId);
            
            // 如果存在对应的话题，删除它
            if (topic != null && topic.getTopicId() != null) {
                communityTopicMapper.deleteCommunityTopicByTopicId(topic.getTopicId());
            }
        }
        
        // 再批量删除鉴定记录本身
        return speciesIdentificationMapper.deleteSpeciesIdentificationByIdentificationIds(identificationIds);
    }

    /**
     * 删除物种鉴定求助信息
     * 
     * @param identificationId 物种鉴定求助主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSpeciesIdentificationByIdentificationId(Long identificationId)
    {
        // 先查询该记录对应的社群话题
        CommunityTopic topic = communityTopicMapper.selectBySourceTypeAndSourceId(2, identificationId);
        
        // 如果存在对应的话题，删除它
        if (topic != null && topic.getTopicId() != null) {
            communityTopicMapper.deleteCommunityTopicByTopicId(topic.getTopicId());
        }
        
        // 再删除鉴定记录本身
        return speciesIdentificationMapper.deleteSpeciesIdentificationByIdentificationId(identificationId);
    }

    /**
     * 审核物种鉴定求助
     * 
     * @param identificationId 鉴定ID
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     * @return 结果
     */
    @Override
    public int audit(Long identificationId, Integer auditStatus, String auditRemark)
    {
        SpeciesIdentification identification = new SpeciesIdentification();
        identification.setIdentificationId(identificationId);
        identification.setAuditStatus(auditStatus);
        identification.setAuditRemark(auditRemark);
        identification.setAuditTime(new Date());
        identification.setAuditBy(SecurityUtils.getUsername());
        identification.setUpdateTime(new Date());
        return speciesIdentificationMapper.updateSpeciesIdentification(identification);
    }
    /**
     * 增加浏览次数
     * 
     * @param identificationId 鉴定ID
     * @return 结果
     */
    @Override
    public int incrementViewCount(Long identificationId)
    {
        return speciesIdentificationMapper.incrementViewCount(identificationId);
    }

    @Override
    public SpeciesIdentification selectSpeciesIdentificationBySharedTopicId(Long sharedTopicId) {
        return speciesIdentificationMapper
                .selectSpeciesIdentificationBySharedTopicId(sharedTopicId);
    }

    /**
     * 设置最佳答案
     * 
     * @param identificationId 鉴定ID
     * @param answerId 答案ID
     * @return 结果
     */
    @Override
    @Transactional
    public int setBestAnswer(Long identificationId, Long answerId)
    {
        // 设置最佳答案标记
        identificationAnswerMapper.setBestAnswer(identificationId, answerId);
        
        // 更新鉴定记录状态为已解决
        return speciesIdentificationMapper.setBestAnswer(identificationId, answerId);
    }

    /**
     * 分享到社群
     *
     * @param identificationId 鉴定ID
     * @param content          分享内容（用户填写，保存到话题 content 字段）
     * @param createBy         当前操作用户名（保存到话题 create_by 字段）
     * @return 社群话题ID
     */
    @Override
    @Transactional
    public Long shareToCommunity(Long identificationId, String content, String createBy)
    {
        SpeciesIdentification identification = speciesIdentificationMapper
                .selectSpeciesIdentificationByIdentificationId(identificationId);

        if (identification == null) {
            throw new RuntimeException("鉴定记录不存在");
        }

        if (Integer.valueOf(1).equals(identification.getIsShared())) {
            throw new RuntimeException("该记录已分享到社群");
        }

        if (!Integer.valueOf(2).equals(identification.getAuditStatus())) {
            throw new RuntimeException("只有审核通过的记录才能分享到社群");
        }

        CommunityTopic topic = new CommunityTopic();
        topic.setUserId(identification.getUserId());
        topic.setUserName(createBy);
        topic.setCategory(2); // 2=鉴定求助板块
        topic.setTitle(identification.getTitle());
        // 使用用户填写的内容，若为空则使用描述信息
        topic.setContent(content != null && !content.trim().isEmpty()
                ? content : (identification.getDescription() != null ? identification.getDescription() : ""));
        // 将鉴定图片同步到话题 images 字段
        topic.setImages(identification.getImages());
        topic.setSourceType(2);
        topic.setSourceId(identificationId);
        topic.setViewCount(0);
        topic.setLikeCount(0);
        topic.setCommentCount(0);
        topic.setCollectCount(0);
        topic.setShareCount(0);
        topic.setIsTop("0");
        topic.setIsEssence("0");
        topic.setStatus("0");
        topic.setAuditStatus(1);
        topic.setCreateBy(createBy);
        topic.setCreateTime(new Date());

        communityTopicMapper.insertCommunityTopic(topic);

        Long topicId = topic.getTopicId();
        if (topicId == null || topicId <= 0) {
            throw new RuntimeException("分享失败：无法获取话题ID");
        }

        identification.setIsShared(1);
        identification.setSharedTopicId(topicId);
        identification.setUpdateTime(new Date());
        speciesIdentificationMapper.updateSpeciesIdentification(identification);

        return topicId;
    }

    /**
     * 构建社群话题内容
     */
    private String buildTopicContent(SpeciesIdentification identification)
    {
        StringBuilder content = new StringBuilder();
        content.append("【物种鉴定求助】\n\n");
        
        if (StringUtils.isNotEmpty(identification.getDescription())) {
            content.append("**详细描述：**\n");
            content.append(identification.getDescription()).append("\n\n");
        }
        
        if (StringUtils.isNotEmpty(identification.getFeatures())) {
            content.append("**特征描述：**\n");
            content.append(identification.getFeatures()).append("\n\n");
        }
        
        if (StringUtils.isNotEmpty(identification.getHabitat())) {
            content.append("**生境描述：**\n");
            content.append(identification.getHabitat()).append("\n\n");
        }
        
        if (StringUtils.isNotEmpty(identification.getLocation())) {
            content.append("**观察地点：**\n");
            content.append(identification.getLocation()).append("\n\n");
        }
        
        if (identification.getObservationTime() != null) {
            content.append("**观察时间：**\n");
            content.append(identification.getObservationTime()).append("\n\n");
        }
        
        content.append("---\n");
        content.append("*来自物种鉴定求助模块*");
        
        return content.toString();
    }
}
