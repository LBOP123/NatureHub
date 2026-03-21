package com.naturalhub.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.naturalhub.system.mapper.ObservationRecordMapper;
import com.naturalhub.system.mapper.CommunityTopicMapper;
import com.naturalhub.system.domain.ObservationRecord;
import com.naturalhub.system.domain.CommunityTopic;
import com.naturalhub.system.service.IObservationRecordService;

/**
 * 观察记录Service业务层处理
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
@Service
public class ObservationRecordServiceImpl implements IObservationRecordService 
{
    @Autowired
    private ObservationRecordMapper observationRecordMapper;

    @Autowired
    private CommunityTopicMapper communityTopicMapper;

    /**
     * 查询观察记录
     * 
     * @param recordId 观察记录主键
     * @return 观察记录
     */
    @Override
    public ObservationRecord selectObservationRecordByRecordId(Long recordId)
    {
        return observationRecordMapper.selectObservationRecordByRecordId(recordId);
    }

    /**
     * 查询观察记录列表
     * 
     * @param observationRecord 观察记录
     * @return 观察记录
     */
    @Override
    public List<ObservationRecord> selectObservationRecordList(ObservationRecord observationRecord)
    {
        return observationRecordMapper.selectObservationRecordList(observationRecord);
    }

    /**
     * 新增观察记录
     * 
     * @param observationRecord 观察记录
     * @return 结果
     */
    @Override
    public int insertObservationRecord(ObservationRecord observationRecord)
    {
        // 默认草稿状态
        if (observationRecord.getAuditStatus() == null) {
            observationRecord.setAuditStatus(0);
        }
        if (observationRecord.getDelFlag() == null) {
            observationRecord.setDelFlag("0");
        }
        if (observationRecord.getIsShared() == null) {
            observationRecord.setIsShared(0);
        }
        return observationRecordMapper.insertObservationRecord(observationRecord);
    }

    /**
     * 修改观察记录
     * 
     * @param observationRecord 观察记录
     * @return 结果
     */
    @Override
    public int updateObservationRecord(ObservationRecord observationRecord)
    {
        return observationRecordMapper.updateObservationRecord(observationRecord);
    }

    /**
     * 批量删除观察记录
     * 
     * @param recordIds 需要删除的观察记录主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteObservationRecordByRecordIds(Long[] recordIds)
    {
        // 逐个删除，每个都检查并删除关联的社群话题
        for (Long recordId : recordIds) {
            // 先查询该记录对应的社群话题
            CommunityTopic topic = communityTopicMapper.selectBySourceTypeAndSourceId(1, recordId);
            
            // 如果存在对应的话题，删除它
            if (topic != null && topic.getTopicId() != null) {
                communityTopicMapper.deleteCommunityTopicByTopicId(topic.getTopicId());
            }
        }
        
        // 再批量删除观察记录本身
        return observationRecordMapper.deleteObservationRecordByRecordIds(recordIds);
    }

    /**
     * 删除观察记录信息
     * 
     * @param recordId 观察记录主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteObservationRecordByRecordId(Long recordId)
    {
        // 先查询该记录对应的社群话题
        CommunityTopic topic = communityTopicMapper.selectBySourceTypeAndSourceId(1, recordId);
        
        // 如果存在对应的话题，删除它
        if (topic != null && topic.getTopicId() != null) {
            communityTopicMapper.deleteCommunityTopicByTopicId(topic.getTopicId());
        }
        
        // 再删除观察记录本身
        return observationRecordMapper.deleteObservationRecordByRecordId(recordId);
    }

    /**
     * 提交审核
     * 
     * @param recordId 观察记录主键
     * @return 结果
     */
    @Override
    public int submitForReview(Long recordId)
    {
        return observationRecordMapper.submitForReview(recordId);
    }

    @Override
    public ObservationRecord selectObservationRecordBySharedTopicId(Long sharedTopicId)
    {
        return observationRecordMapper.selectObservationRecordBySharedTopicId(sharedTopicId);
    }

    /**
     * 分享到社群
     *
     * @param recordId  观察记录主键
     * @param content   分享内容（用户填写，保存到话题 content 字段）
     * @param createBy  当前操作用户名（保存到话题 create_by 字段）
     * @return 社群话题ID
     */
    @Override
    @Transactional
    public Long shareToCommunity(Long recordId, String content, String createBy)
    {
        ObservationRecord record = observationRecordMapper.selectObservationRecordByRecordId(recordId);

        if (record == null) {
            throw new RuntimeException("观察记录不存在");
        }

        if (Integer.valueOf(1).equals(record.getIsShared())) {
            throw new RuntimeException("该记录已分享到社群");
        }

        if (!Integer.valueOf(2).equals(record.getAuditStatus())) {
            throw new RuntimeException("只有审核通过的记录才能分享到社群");
        }

        CommunityTopic topic = new CommunityTopic();
        topic.setUserId(record.getUserId());
        topic.setUserName(createBy);
        topic.setCategory(1);         // 1=观察记录板块
        topic.setTitle(record.getTitle());
        // 使用用户填写的内容，若为空则使用观察描述
        topic.setContent(content != null && !content.trim().isEmpty()
                ? content : (record.getDescription() != null ? record.getDescription() : ""));
        // 将观察记录图片同步到话题 images 字段
        topic.setImages(record.getImages());
        topic.setSourceType(1);
        topic.setSourceId(recordId);
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

        record.setIsShared(1);
        record.setTopicId(topicId);
        record.setSharedTopicId(topicId);
        observationRecordMapper.updateObservationRecord(record);

        return topicId;
    }

}
