package com.naturalhub.system.service;

import java.util.List;
import com.naturalhub.system.domain.CommunityTopic;

/**
 * 社群话题Service接口
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public interface ICommunityTopicService 
{
    /**
     * 查询社群话题
     * 
     * @param topicId 社群话题主键
     * @return 社群话题
     */
    public CommunityTopic selectCommunityTopicByTopicId(Long topicId);

    /**
     * 查询社群话题列表
     * 
     * @param communityTopic 社群话题
     * @return 社群话题集合
     */
    public List<CommunityTopic> selectCommunityTopicList(CommunityTopic communityTopic);

    /**
     * 新增社群话题
     * 
     * @param communityTopic 社群话题
     * @return 结果
     */
    public int insertCommunityTopic(CommunityTopic communityTopic);

    /**
     * 修改社群话题
     * 
     * @param communityTopic 社群话题
     * @return 结果
     */
    public int updateCommunityTopic(CommunityTopic communityTopic);

    /**
     * 批量删除社群话题
     * 
     * @param topicIds 需要删除的社群话题主键集合
     * @return 结果
     */
    public int deleteCommunityTopicByTopicIds(Long[] topicIds);

    /**
     * 删除社群话题信息
     * 
     * @param topicId 社群话题主键
     * @return 结果
     */
    public int deleteCommunityTopicByTopicId(Long topicId);

    /**
     * 增加浏览次数
     * 
     * @param topicId 话题ID
     * @return 结果
     */
    public int incrementViewCount(Long topicId);

    /**
     * 更新话题统计数据
     * 
     * @param topicId 话题ID
     * @return 结果
     */
    public int updateTopicStats(Long topicId);

    /**
     * 点赞话题
     * 
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 结果
     */
    public int likeTopic(Long topicId, Long userId);

    /**
     * 取消点赞话题
     * 
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 结果
     */
    public int unlikeTopic(Long topicId, Long userId);

    /**
     * 收藏话题
     * 
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 结果
     */
    public int collectTopic(Long topicId, Long userId);

    /**
     * 取消收藏话题
     * 
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 结果
     */
    public int uncollectTopic(Long topicId, Long userId);

    /**
     * 置顶/取消置顶话题
     * 
     * @param topicId 话题ID
     * @param isTop 是否置顶
     * @return 结果
     */
    public int setTopicTop(Long topicId, String isTop);

    /**
     * 设置/取消精华话题
     * 
     * @param topicId 话题ID
     * @param isEssence 是否精华
     * @return 结果
     */
    public int setTopicEssence(Long topicId, String isEssence);

    /**
     * 关闭/开启话题
     * 
     * @param topicId 话题ID
     * @param status 状态
     * @return 结果
     */
    public int setTopicStatus(Long topicId, String status);

    int auditTopic(Long topicId, Integer auditStatus, String auditRemark);
}
