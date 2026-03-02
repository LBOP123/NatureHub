package com.naturalhub.system.service;

import java.util.List;
import com.naturalhub.system.domain.CommunityComment;

/**
 * 话题评论Service接口
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public interface ICommunityCommentService 
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
     * 批量删除话题评论
     * 
     * @param commentIds 需要删除的话题评论主键集合
     * @return 结果
     */
    public int deleteCommunityCommentByCommentIds(Long[] commentIds);

    /**
     * 删除话题评论信息
     * 
     * @param commentId 话题评论主键
     * @return 结果
     */
    public int deleteCommunityCommentByCommentId(Long commentId);

    /**
     * 点赞评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 结果
     */
    public int likeComment(Long commentId, Long userId);

    /**
     * 取消点赞评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 结果
     */
    public int unlikeComment(Long commentId, Long userId);
}
