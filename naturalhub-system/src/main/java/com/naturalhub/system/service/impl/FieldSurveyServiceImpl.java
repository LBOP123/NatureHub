package com.naturalhub.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.common.utils.StringUtils;
import com.naturalhub.system.mapper.FieldSurveyMapper;
import com.naturalhub.system.mapper.CommunityTopicMapper;
import com.naturalhub.system.domain.FieldSurvey;
import com.naturalhub.system.domain.CommunityTopic;
import com.naturalhub.system.service.IFieldSurveyService;

/**
 * 野外调查记录Service业务层处理
 *
 * @author NaturalHub
 * @date 2026-03-09
 */
@Service
public class FieldSurveyServiceImpl implements IFieldSurveyService
{
    @Autowired
    private FieldSurveyMapper fieldSurveyMapper;

    @Autowired
    private CommunityTopicMapper communityTopicMapper;

    @Override
    public FieldSurvey selectFieldSurveyBySurveyId(Long surveyId)
    {
        return fieldSurveyMapper.selectFieldSurveyBySurveyId(surveyId);
    }

    @Override
    public List<FieldSurvey> selectFieldSurveyList(FieldSurvey fieldSurvey)
    {
        return fieldSurveyMapper.selectFieldSurveyList(fieldSurvey);
    }

    @Override
    public int insertFieldSurvey(FieldSurvey fieldSurvey)
    {
        // 只在 auditStatus 为 null 时设置默认值 0（草稿）
        if (fieldSurvey.getAuditStatus() == null) {
            fieldSurvey.setAuditStatus(0);
        }
        fieldSurvey.setIsShared(0);
        fieldSurvey.setDelFlag("0");
        fieldSurvey.setCreateTime(new Date());
        return fieldSurveyMapper.insertFieldSurvey(fieldSurvey);
    }

    @Override
    public int updateFieldSurvey(FieldSurvey fieldSurvey)
    {
        fieldSurvey.setUpdateTime(new Date());
        return fieldSurveyMapper.updateFieldSurvey(fieldSurvey);
    }

    @Override
    @Transactional
    public int deleteFieldSurveyBySurveyIds(Long[] surveyIds)
    {
        // 逐个删除，每个都检查并删除关联的社群话题
        for (Long surveyId : surveyIds) {
            // 先查询该记录对应的社群话题
            CommunityTopic topic = communityTopicMapper.selectBySourceTypeAndSourceId(3, surveyId);
            
            // 如果存在对应的话题，删除它
            if (topic != null && topic.getTopicId() != null) {
                communityTopicMapper.deleteCommunityTopicByTopicId(topic.getTopicId());
            }
        }
        
        // 再批量删除野外调查记录本身
        return fieldSurveyMapper.deleteFieldSurveyBySurveyIds(surveyIds);
    }

    @Override
    @Transactional
    public int deleteFieldSurveyBySurveyId(Long surveyId)
    {
        // 先查询该记录对应的社群话题
        CommunityTopic topic = communityTopicMapper.selectBySourceTypeAndSourceId(3, surveyId);
        
        // 如果存在对应的话题，删除它
        if (topic != null && topic.getTopicId() != null) {
            communityTopicMapper.deleteCommunityTopicByTopicId(topic.getTopicId());
        }
        
        // 再删除野外调查记录本身
        return fieldSurveyMapper.deleteFieldSurveyBySurveyId(surveyId);
    }

    /**
     * 审核野外调查记录
     * 【改造点】auditStatus 由字符串改为数字参数
     *   approved -> 2, rejected -> 3, pending -> 1
     *
     * @param surveyId    调查ID
     * @param auditStatus 审核状态（字符串，兼容前端传值，内部转数字）
     * @param auditRemark 审核备注
     */
    @Override
    public int audit(Long surveyId, Integer auditStatus, String auditRemark)
    {
        FieldSurvey survey = new FieldSurvey();
        survey.setSurveyId(surveyId);
        survey.setAuditStatus(auditStatus);
        survey.setAuditRemark(auditRemark);
        survey.setAuditTime(new Date());
        survey.setAuditBy(SecurityUtils.getUsername());
        survey.setUpdateTime(new Date());
        return fieldSurveyMapper.updateFieldSurvey(survey);
    }

    /**
     * 审核状态字符串转数字
     * draft=0, pending=1, approved=2, rejected=3
     */
    /*private Integer convertAuditStatus(String status) {
        if (status == null) return 0;
        switch (status) {
            case "pending":  return 1;
            case "approved": return 2;
            case "rejected": return 3;
            default:         return 0; // draft
        }
    }*/

    @Override
    public FieldSurvey selectFieldSurveyBySharedTopicId(Long sharedTopicId) {
        return fieldSurveyMapper.selectFieldSurveyBySharedTopicId(sharedTopicId);
    }

    /**
     * 分享到社群
     *
     * @param surveyId  野外调查主键
     * @param content   分享内容（用户填写，保存到话题 content 字段）
     * @param createBy  当前操作用户名（保存到话题 create_by 字段）
     * @return 社群话题ID
     */
    @Override
    @Transactional
    public Long shareToCommunity(Long surveyId, String content, String createBy)
    {
        FieldSurvey survey = fieldSurveyMapper.selectFieldSurveyBySurveyId(surveyId);

        if (survey == null) {
            throw new RuntimeException("调查记录不存在");
        }

        if (Integer.valueOf(1).equals(survey.getIsShared())) {
            throw new RuntimeException("该记录已分享到社群");
        }

        if (!Integer.valueOf(2).equals(survey.getAuditStatus())) {
            throw new RuntimeException("只有审核通过的记录才能分享到社群");
        }

        // 创建社群话题
        CommunityTopic topic = new CommunityTopic();
        topic.setUserId(survey.getUserId());
        topic.setUserName(createBy);
        topic.setCategory(3);         // 3=野外调查板块
        topic.setTitle(survey.getTitle());
        // 使用用户填写的内容，若为空则回退到调查描述
        topic.setContent(content != null && !content.trim().isEmpty()
                ? content : (survey.getDescription() != null ? survey.getDescription() : ""));
        // 将野外调查图片同步到话题 images 字段
        topic.setImages(survey.getImages());
        topic.setSourceType(3);
        topic.setSourceId(surveyId);
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

        survey.setIsShared(1);
        survey.setSharedTopicId(topicId);
        survey.setUpdateTime(new Date());
        fieldSurveyMapper.updateFieldSurvey(survey);

        return topicId;
    }
}
