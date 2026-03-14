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
import com.naturalhub.system.domain.ObservationRecord;
import com.naturalhub.system.domain.SpeciesIdentification;
import com.naturalhub.system.domain.FieldSurvey;
import com.naturalhub.system.service.ICommunityTopicService;
import com.naturalhub.system.service.ICommunityCommentService;
import com.naturalhub.system.service.IObservationRecordService;
import com.naturalhub.system.service.ISpeciesIdentificationService;
import com.naturalhub.system.service.IFieldSurveyService;
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

    @Autowired
    private IObservationRecordService observationRecordService;

    @Autowired
    private ISpeciesIdentificationService speciesIdentificationService;

    @Autowired
    private IFieldSurveyService fieldSurveyService;

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
        Integer auditStatus = topic.getAuditStatus();
        boolean approved = auditStatus != null && auditStatus == 1;
        Long currentUserId = SecurityUtils.getUserId();
        if (!approved && (topic.getUserId() == null || !topic.getUserId().equals(currentUserId)))
        {
            return error("话题审核中或未通过，暂不可查看");
        }
        
        communityTopicService.incrementViewCount(topicId);
        
        topic.setIsLiked(communityTopicMapper.checkUserLike(topicId, currentUserId) > 0);
        topic.setIsCollected(communityTopicMapper.checkUserCollect(topicId, currentUserId) > 0);
        // 兼容历史数据：自动修正 sourceType 与 category 不匹配的问题
        if (topic.getSourceType() != null && topic.getSourceType() != 0) {
            java.util.Map<Integer,Integer> catMap = new java.util.HashMap<>();
            catMap.put(1, 0); // observation -> species_science
            catMap.put(2, 2); // identification -> identify_help
            catMap.put(3, 1); // survey -> field_explore
            Integer correct = catMap.get(topic.getSourceType());
            if (correct != null && !correct.equals(topic.getCategory())) {
                topic.setCategory(correct);
                CommunityTopic fix = new CommunityTopic();
                fix.setTopicId(topicId); fix.setCategory(correct);
                communityTopicService.updateCommunityTopic(fix);
            }
        }
        // 兼容历史数据：sourceType 与 category 不匹配时自动修正并写回
        if (topic.getSourceType() != null) {
            Integer correctCategory = null;
            switch (topic.getSourceType()) {
                case 1: correctCategory = 0; break; // observation -> species_science
                case 2: correctCategory = 2; break; // identification -> identify_help
                case 3: correctCategory = 1; break; // survey -> field_explore
                default: break;
            }
            if (correctCategory != null && !correctCategory.equals(topic.getCategory())) {
                topic.setCategory(correctCategory);
                CommunityTopic fix = new CommunityTopic();
                fix.setTopicId(topicId);
                fix.setCategory(correctCategory);
                communityTopicService.updateCommunityTopic(fix);
            }
        }

        // 不在服务端联查原始记录，由前端按 sourceType+sourceId 动态请求对应业务接口
        return success(topic);
    }

    /**
     * 查询原始业务记录详情（供话题详情页动态加载）
     * GET /user/community/source/{type}/{topicId}
     * type: 1=observation观察记录 | 2=identification物种鉴定 | 3=survey野外调查
     * 或兼容旧格式: observation | identification | survey
     * topicId: 社群话题ID（通过 source_id 或 shared_topic_id 反查原始记录）
     */
    @GetMapping("/source/{type}/{topicId}")
    public AjaxResult getSourceDetail(@PathVariable("type") String type,
                                      @PathVariable("topicId") Long topicId)
    {
        try {
            // 先尝试从话题获取 sourceId，有则直接按主键查
            CommunityTopic topic = communityTopicService.selectCommunityTopicByTopicId(topicId);
            
            // 兼容新旧格式：整数 1/2/3 或字符串 observation/identification/survey
            boolean isObservation = "1".equals(type) || "observation".equals(type);
            boolean isIdentification = "2".equals(type) || "identification".equals(type);
            boolean isSurvey = "3".equals(type) || "survey".equals(type);
            
            if (isObservation) {
                ObservationRecord rec = null;
                if (topic != null && topic.getSourceId() != null) {
                    rec = observationRecordService.selectObservationRecordByRecordId(topic.getSourceId());
                }
                if (rec == null) {
                    rec = observationRecordService.selectObservationRecordBySharedTopicId(topicId);
                }
                return rec != null ? success(rec) : error("原始观察记录不存在");
            } else if (isIdentification) {
                SpeciesIdentification rec = null;
                if (topic != null && topic.getSourceId() != null) {
                    rec = speciesIdentificationService.selectSpeciesIdentificationByIdentificationId(topic.getSourceId());
                }
                if (rec == null) {
                    rec = speciesIdentificationService.selectSpeciesIdentificationBySharedTopicId(topicId);
                }
                return rec != null ? success(rec) : error("原始鉴定记录不存在");
            } else if (isSurvey) {
                FieldSurvey rec = null;
                if (topic != null && topic.getSourceId() != null) {
                    rec = fieldSurveyService.selectFieldSurveyBySurveyId(topic.getSourceId());
                }
                if (rec == null) {
                    rec = fieldSurveyService.selectFieldSurveyBySharedTopicId(topicId);
                }
                return rec != null ? success(rec) : error("原始调查记录不存在");
            } else {
                return error("不支持的来源类型: " + type);
            }
        } catch (Exception e) {
            return error("获取原始记录失败: " + e.getMessage());
        }
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
            communityTopic.setAuditStatus(1);
        }
        List<CommunityTopic> list = communityTopicService.selectCommunityTopicList(communityTopic);
        return getDataTable(list);
    }
}
