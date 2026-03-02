package com.naturalhub.system.mapper;

import java.util.List;
import com.naturalhub.system.domain.CommunityComment;
import org.apache.ibatis.annotations.Param;

/**
 * 话题评论Mapper接口
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public interface CommunityCommentMapper 
{
    /**
     * 查询话题评论
     * 
     * @param commentId 话题评论主键
     * @return 话题评论
     */
    public CommunityComment selectCommunityCommentByCommentId(Long commentId);

    /**
     * 查询话题评论列表
     * 
     * @param communityComment 话题评论
     * @return 话题评论集合
     */
    public List<CommunityComment> selectCommunityCommentList(CommunityComment communityComment);

    /**
     * 新增话题评论
     * 
     * @param communityComment 话题评论
     * @return 结果
     */
    public int insertCommunityComment(CommunityComment communityComment);

    /**
     * 修改话题评论
     * 
     * @param communityComment 话题评论
     * @return 结果
     */
    public int updateCommunityComment(CommunityComment communityComment);

    /**
     * 删除话题评论
     * 
     * @param commentId 话题评论主键
     * @return 结果
     */
    public int deleteCommunityCommentByCommentId(Long commentId);

    /**
     * 批量删除话题评论
     * 
     * @param commentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCommunityCommentByCommentIds(Long[] commentIds);

    /**
     * 根据话题ID查询评论数
     * 
     * @param topicId 话题ID
     * @return 评论数
     */
    public int countCommentsByTopicId(Long topicId);

    /**
     * 检查用户是否点赞评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 结果
     */
    public int checkUserLikeComment(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
