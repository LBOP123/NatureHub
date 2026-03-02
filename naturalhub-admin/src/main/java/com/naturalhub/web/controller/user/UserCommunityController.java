package com.naturalhub.web.controller.user;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.naturalhub.common.annotation.Log;
import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.enums.BusinessType;
import com.naturalhub.system.domain.CommunityTopic;
import com.naturalhub.system.domain.CommunityComment;
import com.naturalhub.system.service.ICommunityTopicService;
import com.naturalhub.system.service.ICommunityCommentService;
import com.naturalhub.system.mapper.CommunityTopicMapper;
import com.naturalhub.system.mapper.CommunityCommentMapper;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.common.utils.SecurityUtils;

/**
 * 社群话题Controller（用户端）
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
@RestController
@RequestMapping("/user/community")
public class UserCommunityController extends BaseController
{
    @Autowired
    private ICommunityTopicService communityTopicService;

    @Autowired
    private ICommunityCommentService communityCommentService;

    @Autowired
    private CommunityTopicMapper communityTopicMapper;

    @Autowired
    private CommunityCommentMapper communityCommentMapper;

    /**
     * 查询社群话题列表（用户端）
     */
    @GetMapping("/topic/list")
    public TableDataInfo list(CommunityTopic communityTopic)
    {
        startPage();
        communityTopic.setStatus("0");
        List<CommunityTopic> list = communityTopicService.selectCommunityTopicList(communityTopic);
        
        Long userId = SecurityUtils.getUserId();
        for (CommunityTopic topic : list) {
            topic.setIsLiked(communityTopicMapper.checkUserLike(topic.getTopicId(), userId) > 0);
            topic.setIsCollected(communityTopicMapper.checkUserCollect(topic.getTopicId(), userId) > 0);
        }
        
        return getDataTable(list);
    }

    /**
     * 获取社群话题详细信息
     */
    @GetMapping(value = "/topic/{topicId}")
    public AjaxResult getInfo(@PathVariable("topicId") Long topicId)
    {
        CommunityTopic topic = communityTopicService.selectCommunityTopicByTopicId(topicId);
        if (topic == null || !"0".equals(topic.getStatus())) {
            return error("话题不存在或已关闭");
        }

        // 未审核通过的话题，仅允许作者本人查看
        String auditStatus = topic.getAuditStatus();
        boolean approved = "1".equals(auditStatus) || "approved".equalsIgnoreCase(auditStatus);
        Long currentUserId = SecurityUtils.getUserId();
        if (!approved && (topic.getUserId() == null || !topic.getUserId().equals(currentUserId)))
        {
            return error("话题审核中或未通过，暂不可查看");
        }
        
        communityTopicService.incrementViewCount(topicId);
        
        topic.setIsLiked(communityTopicMapper.checkUserLike(topicId, currentUserId) > 0);
        topic.setIsCollected(communityTopicMapper.checkUserCollect(topicId, currentUserId) > 0);
        
        return success(topic);
    }

    /**
     * 新增社群话题
     */
    @Log(title = "社群话题", businessType = BusinessType.INSERT)
    @PostMapping("/topic")
    public AjaxResult add(@RequestBody CommunityTopic communityTopic)
    {
        communityTopic.setUserId(SecurityUtils.getUserId());
        communityTopic.setUserName(SecurityUtils.getUsername());
        communityTopic.setCreateBy(SecurityUtils.getUsername());
        communityTopic.setStatus("0");
        communityTopic.setIsTop("0");
        communityTopic.setIsEssence("0");
        return toAjax(communityTopicService.insertCommunityTopic(communityTopic));
    }

    /**
     * 修改社群话题
     */
    @Log(title = "社群话题", businessType = BusinessType.UPDATE)
    @PutMapping("/topic")
    public AjaxResult edit(@RequestBody CommunityTopic communityTopic)
    {
        CommunityTopic existTopic = communityTopicService.selectCommunityTopicByTopicId(communityTopic.getTopicId());
        if (existTopic == null || !existTopic.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权修改该话题");
        }
        communityTopic.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(communityTopicService.updateCommunityTopic(communityTopic));
    }

    /**
     * 删除社群话题
     */
    @Log(title = "社群话题", businessType = BusinessType.DELETE)
	@DeleteMapping("/topic/{topicIds}")
    public AjaxResult remove(@PathVariable Long[] topicIds)
    {
        Long userId = SecurityUtils.getUserId();
        for (Long topicId : topicIds) {
            CommunityTopic topic = communityTopicService.selectCommunityTopicByTopicId(topicId);
            if (topic == null || !topic.getUserId().equals(userId)) {
                return error("无权删除该话题");
            }
        }
        return toAjax(communityTopicService.deleteCommunityTopicByTopicIds(topicIds));
    }

    /**
     * 点赞话题
     */
    @PostMapping("/topic/like/{topicId}")
    public AjaxResult likeTopic(@PathVariable Long topicId)
    {
        try {
            return toAjax(communityTopicService.likeTopic(topicId, SecurityUtils.getUserId()));
        } catch (Exception e) {
            return error("已经点赞过了");
        }
    }

    /**
     * 取消点赞话题
     */
    @DeleteMapping("/topic/like/{topicId}")
    public AjaxResult unlikeTopic(@PathVariable Long topicId)
    {
        return toAjax(communityTopicService.unlikeTopic(topicId, SecurityUtils.getUserId()));
    }

    /**
     * 收藏话题
     */
    @PostMapping("/topic/collect/{topicId}")
    public AjaxResult collectTopic(@PathVariable Long topicId)
    {
        try {
            return toAjax(communityTopicService.collectTopic(topicId, SecurityUtils.getUserId()));
        } catch (Exception e) {
            return error("已经收藏过了");
        }
    }

    /**
     * 取消收藏话题
     */
    @DeleteMapping("/topic/collect/{topicId}")
    public AjaxResult uncollectTopic(@PathVariable Long topicId)
    {
        return toAjax(communityTopicService.uncollectTopic(topicId, SecurityUtils.getUserId()));
    }

    /**
     * 查询话题评论列表
     */
    @GetMapping("/comment/list")
    public TableDataInfo commentList(CommunityComment communityComment)
    {
        startPage();
        communityComment.setStatus("0");
        List<CommunityComment> list = communityCommentService.selectCommunityCommentList(communityComment);
        
        Long userId = SecurityUtils.getUserId();
        for (CommunityComment comment : list) {
            comment.setIsLiked(communityCommentMapper.checkUserLikeComment(comment.getCommentId(), userId) > 0);
        }
        
        return getDataTable(list);
    }

    /**
     * 新增评论
     */
    @PostMapping("/comment")
    public AjaxResult addComment(@RequestBody CommunityComment communityComment)
    {
        communityComment.setUserId(SecurityUtils.getUserId());
        communityComment.setUserName(SecurityUtils.getUsername());
        communityComment.setCreateBy(SecurityUtils.getUsername());
        communityComment.setStatus("0");
        if (communityComment.getParentId() == null) {
            communityComment.setParentId(0L);
        }
        return toAjax(communityCommentService.insertCommunityComment(communityComment));
    }

    /**
     * 删除评论
     */
	@DeleteMapping("/comment/{commentIds}")
    public AjaxResult removeComment(@PathVariable Long[] commentIds)
    {
        Long userId = SecurityUtils.getUserId();
        for (Long commentId : commentIds) {
            CommunityComment comment = communityCommentService.selectCommunityCommentByCommentId(commentId);
            if (comment == null || !comment.getUserId().equals(userId)) {
                return error("无权删除该评论");
            }
        }
        return toAjax(communityCommentService.deleteCommunityCommentByCommentIds(commentIds));
    }

    /**
     * 点赞评论
     */
    @PostMapping("/comment/like/{commentId}")
    public AjaxResult likeComment(@PathVariable Long commentId)
    {
        try {
            return toAjax(communityCommentService.likeComment(commentId, SecurityUtils.getUserId()));
        } catch (Exception e) {
            return error("已经点赞过了");
        }
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/comment/like/{commentId}")
    public AjaxResult unlikeComment(@PathVariable Long commentId)
    {
        return toAjax(communityCommentService.unlikeComment(commentId, SecurityUtils.getUserId()));
    }

    /**
     * 我的话题列表
     */
    @GetMapping("/my/topic")
    public TableDataInfo myTopic(CommunityTopic communityTopic)
    {
        startPage();
        Long currentUserId = SecurityUtils.getUserId();
        Long queryUserId = communityTopic.getUserId();
        // 默认查自己；若查他人，仅返回审核通过的话题
        if (queryUserId == null || currentUserId.equals(queryUserId))
        {
            communityTopic.setUserId(currentUserId);
        }
        else
        {
            communityTopic.setUserId(queryUserId);
            communityTopic.setStatus("0");
            communityTopic.setAuditStatus("1");
        }
        List<CommunityTopic> list = communityTopicService.selectCommunityTopicList(communityTopic);
        return getDataTable(list);
    }
}
