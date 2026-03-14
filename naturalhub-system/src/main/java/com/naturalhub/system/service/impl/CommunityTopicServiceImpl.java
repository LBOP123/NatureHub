package com.naturalhub.system.service.impl;

import java.util.Date;
import java.util.List;
import com.naturalhub.common.utils.DateUtils;
import com.naturalhub.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.naturalhub.system.mapper.CommunityTopicMapper;
import com.naturalhub.system.mapper.CommunityInteractionMapper;
import com.naturalhub.system.domain.CommunityTopic;
import com.naturalhub.system.service.ICommunityTopicService;

/**
 * 社群话题Service业务层处理
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
@Service
public class CommunityTopicServiceImpl implements ICommunityTopicService 
{
    @Autowired
    private CommunityTopicMapper communityTopicMapper;

    @Autowired
    private CommunityInteractionMapper communityInteractionMapper;

    @Override
    public CommunityTopic selectCommunityTopicByTopicId(Long topicId)
    {
        return communityTopicMapper.selectCommunityTopicByTopicId(topicId);
    }

    @Override
    public List<CommunityTopic> selectCommunityTopicList(CommunityTopic communityTopic)
    {
        return communityTopicMapper.selectCommunityTopicList(communityTopic);
    }

    @Override
    public int insertCommunityTopic(CommunityTopic communityTopic)
    {
        communityTopic.setCreateTime(DateUtils.getNowDate());
        communityTopic.setAuditStatus(0); // 0=待审核 // 默认待审核
        return communityTopicMapper.insertCommunityTopic(communityTopic);
    }

    @Override
    public int updateCommunityTopic(CommunityTopic communityTopic)
    {
        communityTopic.setUpdateTime(DateUtils.getNowDate());
        return communityTopicMapper.updateCommunityTopic(communityTopic);
    }

    @Override
    public int deleteCommunityTopicByTopicIds(Long[] topicIds)
    {
        return communityTopicMapper.deleteCommunityTopicByTopicIds(topicIds);
    }

    @Override
    public int deleteCommunityTopicByTopicId(Long topicId)
    {
        return communityTopicMapper.deleteCommunityTopicByTopicId(topicId);
    }

    @Override
    public int incrementViewCount(Long topicId)
    {
        return communityTopicMapper.incrementViewCount(topicId);
    }

    @Override
    public int updateTopicStats(Long topicId)
    {
        return communityTopicMapper.updateTopicStats(topicId);
    }

    @Override
    @Transactional
    public int likeTopic(Long topicId, Long userId)
    {
        int result = communityInteractionMapper.insertLike(userId, "topic", topicId);
        if (result > 0) {
            updateTopicStats(topicId);
        }
        return result;
    }

    @Override
    @Transactional
    public int unlikeTopic(Long topicId, Long userId)
    {
        int result = communityInteractionMapper.deleteLike(userId, "topic", topicId);
        if (result > 0) {
            updateTopicStats(topicId);
        }
        return result;
    }

    @Override
    @Transactional
    public int collectTopic(Long topicId, Long userId)
    {
        int result = communityInteractionMapper.insertCollect(userId, topicId);
        if (result > 0) {
            updateTopicStats(topicId);
        }
        return result;
    }

    @Override
    @Transactional
    public int uncollectTopic(Long topicId, Long userId)
    {
        int result = communityInteractionMapper.deleteCollect(userId, topicId);
        if (result > 0) {
            updateTopicStats(topicId);
        }
        return result;
    }

    @Override
    public int setTopicTop(Long topicId, String isTop)
    {
        CommunityTopic topic = new CommunityTopic();
        topic.setTopicId(topicId);
        topic.setIsTop(isTop);
        return communityTopicMapper.updateCommunityTopic(topic);
    }

    @Override
    public int setTopicEssence(Long topicId, String isEssence)
    {
        CommunityTopic topic = new CommunityTopic();
        topic.setTopicId(topicId);
        topic.setIsEssence(isEssence);
        return communityTopicMapper.updateCommunityTopic(topic);
    }

    @Override
    public int setTopicStatus(Long topicId, String status)
    {
        CommunityTopic topic = new CommunityTopic();
        topic.setTopicId(topicId);
        topic.setStatus(status);
        return communityTopicMapper.updateCommunityTopic(topic);
    }

    @Override
    public int auditTopic(Long topicId, Integer auditStatus, String auditRemark)
    {
        CommunityTopic topic = communityTopicMapper.selectCommunityTopicByTopicId(topicId);
        if (topic == null)
        {
            return 0;
        }

        topic.setAuditStatus(auditStatus);
        topic.setAuditRemark(auditRemark);
        topic.setAuditTime(new Date());
        topic.setAuditBy(SecurityUtils.getUsername());

        return communityTopicMapper.updateCommunityTopic(topic);
    }
}
