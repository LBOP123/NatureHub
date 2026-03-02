package com.naturalhub.system.mapper;

import java.util.List;
import com.naturalhub.system.domain.CommunityTopic;
import org.apache.ibatis.annotations.Param;

/**
 * 社群话题Mapper接口
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public interface CommunityTopicMapper 
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
     * 删除社群话题
     * 
     * @param topicId 社群话题主键
     * @return 结果
     */
    public int deleteCommunityTopicByTopicId(Long topicId);

    /**
     * 批量删除社群话题
     * 
     * @param topicIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCommunityTopicByTopicIds(Long[] topicIds);

    /**
     * 增加浏览次数
     * 
     * @param topicId 话题ID
     * @return 结果
     */
    public int incrementViewCount(Long topicId);

    /**
     * 更新统计数据
     * 
     * @param topicId 话题ID
     * @return 结果
     */
    public int updateTopicStats(Long topicId);

    /**
     * 检查用户是否点赞
     * 
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 结果
     */
    public int checkUserLike(@Param("topicId") Long topicId, @Param("userId") Long userId);

    /**
     * 检查用户是否收藏
     * 
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 结果
     */
    public int checkUserCollect(@Param("topicId") Long topicId, @Param("userId") Long userId);
}
