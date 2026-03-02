package com.naturalhub.system.service.impl;

import java.util.List;
import com.naturalhub.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.naturalhub.system.mapper.CommunityCommentMapper;
import com.naturalhub.system.mapper.CommunityInteractionMapper;
import com.naturalhub.system.mapper.CommunityTopicMapper;
import com.naturalhub.system.domain.CommunityComment;
import com.naturalhub.system.service.ICommunityCommentService;

/**
 * 话题评论Service业务层处理
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
@Service
public class CommunityCommentServiceImpl implements ICommunityCommentService 
{
    @Autowired
    private CommunityCommentMapper communityCommentMapper;

    @Autowired
    private CommunityInteractionMapper communityInteractionMapper;

    @Autowired
    private CommunityTopicMapper communityTopicMapper;

    /**
     * 查询话题评论
     * 
     * @param commentId 话题评论主键
     * @return 话题评论
     */
    @Override
    public CommunityComment selectCommunityCommentByCommentId(Long commentId)
    {
        return communityCommentMapper.selectCommunityCommentByCommentId(commentId);
    }

    /**
     * 查询话题评论列表
     * 
     * @param communityComment 话题评论
     * @return 话题评论
     */
    @Override
    public List<CommunityComment> selectCommunityCommentList(CommunityComment communityComment)
    {
        return communityCommentMapper.selectCommunityCommentList(communityComment);
    }

    /**
     * 新增话题评论
     * 
     * @param communityComment 话题评论
     * @return 结果
     */
    @Override
    @Transactional
    public int insertCommunityComment(CommunityComment communityComment)
    {
        communityComment.setCreateTime(DateUtils.getNowDate());
        int result = communityCommentMapper.insertCommunityComment(communityComment);
        if (result > 0) {
            // 更新话题统计
            communityTopicMapper.updateTopicStats(communityComment.getTopicId());
        }
        return result;
    }

    /**
     * 修改话题评论
     * 
     * @param communityComment 话题评论
     * @return 结果
     */
    @Override
    public int updateCommunityComment(CommunityComment communityComment)
    {
        communityComment.setUpdateTime(DateUtils.getNowDate());
        return communityCommentMapper.updateCommunityComment(communityComment);
    }

    /**
     * 批量删除话题评论
     * 
     * @param commentIds 需要删除的话题评论主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteCommunityCommentByCommentIds(Long[] commentIds)
    {
        int result = communityCommentMapper.deleteCommunityCommentByCommentIds(commentIds);
        // 更新相关话题统计
        for (Long commentId : commentIds) {
            CommunityComment comment = communityCommentMapper.selectCommunityCommentByCommentId(commentId);
            if (comment != null) {
                communityTopicMapper.updateTopicStats(comment.getTopicId());
            }
        }
        return result;
    }

    /**
     * 删除话题评论信息
     * 
     * @param commentId 话题评论主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteCommunityCommentByCommentId(Long commentId)
    {
        CommunityComment comment = communityCommentMapper.selectCommunityCommentByCommentId(commentId);
        int result = communityCommentMapper.deleteCommunityCommentByCommentId(commentId);
        if (result > 0 && comment != null) {
            // 更新话题统计
            communityTopicMapper.updateTopicStats(comment.getTopicId());
        }
        return result;
    }

    /**
     * 点赞评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int likeComment(Long commentId, Long userId)
    {
        int result = communityInteractionMapper.insertLike(userId, "comment", commentId);
        if (result > 0) {
            // 更新评论点赞数
            CommunityComment comment = communityCommentMapper.selectCommunityCommentByCommentId(commentId);
            if (comment != null) {
                comment.setLikeCount(comment.getLikeCount() + 1);
                communityCommentMapper.updateCommunityComment(comment);
            }
        }
        return result;
    }

    /**
     * 取消点赞评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int unlikeComment(Long commentId, Long userId)
    {
        int result = communityInteractionMapper.deleteLike(userId, "comment", commentId);
        if (result > 0) {
            // 更新评论点赞数
            CommunityComment comment = communityCommentMapper.selectCommunityCommentByCommentId(commentId);
            if (comment != null && comment.getLikeCount() > 0) {
                comment.setLikeCount(comment.getLikeCount() - 1);
                communityCommentMapper.updateCommunityComment(comment);
            }
        }
        return result;
    }
}
