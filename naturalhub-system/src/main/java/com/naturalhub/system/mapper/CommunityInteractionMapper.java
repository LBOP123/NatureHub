package com.naturalhub.system.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 社群互动Mapper接口（点赞、收藏）
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public interface CommunityInteractionMapper 
{
    /**
     * 添加点赞
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 结果
     */
    public int insertLike(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 取消点赞
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 结果
     */
    public int deleteLike(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 添加收藏
     * 
     * @param userId 用户ID
     * @param topicId 话题ID
     * @return 结果
     */
    public int insertCollect(@Param("userId") Long userId, @Param("topicId") Long topicId);

    /**
     * 取消收藏
     * 
     * @param userId 用户ID
     * @param topicId 话题ID
     * @return 结果
     */
    public int deleteCollect(@Param("userId") Long userId, @Param("topicId") Long topicId);

    /**
     * 统计点赞数
     * 
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 点赞数
     */
    public int countLikes(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 统计收藏数
     * 
     * @param topicId 话题ID
     * @return 收藏数
     */
    public int countCollects(@Param("topicId") Long topicId);
}
