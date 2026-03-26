package com.naturalhub.system.service.impl;

import java.util.Date;
import java.util.List;

import com.naturalhub.common.core.domain.entity.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.common.utils.StringUtils;
import com.naturalhub.common.utils.baidu.BaiduAiUtil;
import com.naturalhub.system.mapper.SpeciesIdentificationMapper;
import com.naturalhub.system.mapper.IdentificationAnswerMapper;
import com.naturalhub.system.mapper.CommunityTopicMapper;
import com.naturalhub.system.mapper.IdentificationVoteMapper;
import com.naturalhub.system.mapper.SysUserMapper;
import com.naturalhub.system.domain.SpeciesIdentification;
import com.naturalhub.system.domain.CommunityTopic;
import com.naturalhub.system.domain.IdentificationVote;
import com.naturalhub.common.core.domain.entity.SysUser;
import com.naturalhub.system.service.ISpeciesIdentificationService;
import com.naturalhub.system.service.IIdentificationVoteService;

/**
 * 物种鉴定求助Service业务层处理
 *
 * @author NaturalHub
 * @date 2026-03-09
 */
@Service
public class SpeciesIdentificationServiceImpl implements ISpeciesIdentificationService
{
    private static final Logger log = LoggerFactory.getLogger(SpeciesIdentificationServiceImpl.class);

    @Autowired
    private SpeciesIdentificationMapper speciesIdentificationMapper;

    @Autowired
    private IdentificationAnswerMapper identificationAnswerMapper;

    @Autowired
    private CommunityTopicMapper communityTopicMapper;

    @Autowired
    private IdentificationVoteMapper voteMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private IIdentificationVoteService voteService;

    @Autowired
    private BaiduAiUtil baiduAiUtil;

    /**
     * 查询物种鉴定求助
     * 
     * @param identificationId 物种鉴定求助主键
     * @return 物种鉴定求助
     */
    @Override
    public SpeciesIdentification selectSpeciesIdentificationByIdentificationId(Long identificationId)
    {
        return speciesIdentificationMapper.selectSpeciesIdentificationByIdentificationId(identificationId);
    }

    /**
     * 查询物种鉴定求助列表
     * 
     * @param speciesIdentification 物种鉴定求助
     * @return 物种鉴定求助
     */
    @Override
    public List<SpeciesIdentification> selectSpeciesIdentificationList(SpeciesIdentification speciesIdentification)
    {
        return speciesIdentificationMapper.selectSpeciesIdentificationList(speciesIdentification);
    }

    /**
     * 新增物种鉴定求助
     * 
     * @param speciesIdentification 物种鉴定求助
     * @return 结果
     */
    @Override
    public int insertSpeciesIdentification(SpeciesIdentification speciesIdentification)
    {
        speciesIdentification.setStatus(0); // 0=待鉴定
        // 保留前端传入的 auditStatus（0=草稿,1=待审核）
        if (speciesIdentification.getAuditStatus() == null) {
            speciesIdentification.setAuditStatus(0);
        }
        speciesIdentification.setAnswerCount(0);
        speciesIdentification.setViewCount(0);
        speciesIdentification.setIsShared(0); // 0=否
        speciesIdentification.setDelFlag("0");
        speciesIdentification.setCreateTime(new Date());
        return speciesIdentificationMapper.insertSpeciesIdentification(speciesIdentification);
    }

    /**
     * 修改物种鉴定求助
     * 
     * @param speciesIdentification 物种鉴定求助
     * @return 结果
     */
    @Override
    public int updateSpeciesIdentification(SpeciesIdentification speciesIdentification)
    {
        speciesIdentification.setUpdateTime(new Date());
        return speciesIdentificationMapper.updateSpeciesIdentification(speciesIdentification);
    }

    /**
     * 批量删除物种鉴定求助
     * 
     * @param identificationIds 需要删除的物种鉴定求助主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSpeciesIdentificationByIdentificationIds(Long[] identificationIds)
    {
        // 逐个删除，每个都检查并删除关联的社群话题
        for (Long identificationId : identificationIds) {
            // 先查询该记录对应的社群话题
            CommunityTopic topic = communityTopicMapper.selectBySourceTypeAndSourceId(2, identificationId);
            
            // 如果存在对应的话题，删除它
            if (topic != null && topic.getTopicId() != null) {
                communityTopicMapper.deleteCommunityTopicByTopicId(topic.getTopicId());
            }
        }
        
        // 再批量删除鉴定记录本身
        return speciesIdentificationMapper.deleteSpeciesIdentificationByIdentificationIds(identificationIds);
    }

    /**
     * 删除物种鉴定求助信息
     * 
     * @param identificationId 物种鉴定求助主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSpeciesIdentificationByIdentificationId(Long identificationId)
    {
        // 先查询该记录对应的社群话题
        CommunityTopic topic = communityTopicMapper.selectBySourceTypeAndSourceId(2, identificationId);
        
        // 如果存在对应的话题，删除它
        if (topic != null && topic.getTopicId() != null) {
            communityTopicMapper.deleteCommunityTopicByTopicId(topic.getTopicId());
        }
        
        // 再删除鉴定记录本身
        return speciesIdentificationMapper.deleteSpeciesIdentificationByIdentificationId(identificationId);
    }

    /**
     * 审核物种鉴定求助
     * 
     * @param identificationId 鉴定ID
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     * @return 结果
     */
    @Override
    public int audit(Long identificationId, Integer auditStatus, String auditRemark)
    {
        SpeciesIdentification identification = new SpeciesIdentification();
        identification.setIdentificationId(identificationId);
        identification.setAuditStatus(auditStatus);
        identification.setAuditRemark(auditRemark);
        identification.setAuditTime(new Date());
        identification.setAuditBy(SecurityUtils.getUsername());
        identification.setUpdateTime(new Date());
        return speciesIdentificationMapper.updateSpeciesIdentification(identification);
    }
    /**
     * 增加浏览次数
     * 
     * @param identificationId 鉴定ID
     * @return 结果
     */
    @Override
    public int incrementViewCount(Long identificationId)
    {
        return speciesIdentificationMapper.incrementViewCount(identificationId);
    }

    @Override
    public SpeciesIdentification selectSpeciesIdentificationBySharedTopicId(Long sharedTopicId) {
        return speciesIdentificationMapper
                .selectSpeciesIdentificationBySharedTopicId(sharedTopicId);
    }

    /**
     * 设置最佳答案
     * 
     * @param identificationId 鉴定ID
     * @param answerId 答案ID
     * @return 结果
     */
    @Override
    @Transactional
    public int setBestAnswer(Long identificationId, Long answerId)
    {
        // 设置最佳答案标记
        identificationAnswerMapper.setBestAnswer(identificationId, answerId);
        
        // 更新鉴定记录状态为已解决
        return speciesIdentificationMapper.setBestAnswer(identificationId, answerId);
    }

    /**
     * 分享到社群
     *
     * @param identificationId 鉴定ID
     * @param content          分享内容（用户填写，保存到话题 content 字段）
     * @param createBy         当前操作用户名（保存到话题 create_by 字段）
     * @return 社群话题ID
     */
    @Override
    @Transactional
    public Long shareToCommunity(Long identificationId, String content, String createBy)
    {
        SpeciesIdentification identification = speciesIdentificationMapper
                .selectSpeciesIdentificationByIdentificationId(identificationId);

        if (identification == null) {
            throw new RuntimeException("鉴定记录不存在");
        }

        if (Integer.valueOf(1).equals(identification.getIsShared())) {
            throw new RuntimeException("该记录已分享到社群");
        }

        if (!Integer.valueOf(2).equals(identification.getAuditStatus())) {
            throw new RuntimeException("只有审核通过的记录才能分享到社群");
        }

        CommunityTopic topic = new CommunityTopic();
        topic.setUserId(identification.getUserId());
        topic.setUserName(createBy);
        topic.setCategory(2); // 2=鉴定求助板块
        topic.setTitle(identification.getTitle());
        // 使用用户填写的内容，若为空则使用描述信息
        topic.setContent(content != null && !content.trim().isEmpty()
                ? content : (identification.getDescription() != null ? identification.getDescription() : ""));
        // 将鉴定图片同步到话题 images 字段
        topic.setImages(identification.getImages());
        topic.setSourceType(2);
        topic.setSourceId(identificationId);
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

        // 更新鉴定记录：设置已分享，状态改为鉴定中(1)，初始化投票状态
        identification.setIsShared(1);
        identification.setSharedTopicId(topicId);
        identification.setStatus(1); // 1=鉴定中（投票中）
        identification.setVoteStatus("1"); // 1=进行中
        identification.setVoteResult("2"); // 2=未确定，初始结果
        identification.setVoteAgreeScore(0);
        identification.setVoteDisagreeScore(0);
        identification.setUpdateTime(new Date());
        speciesIdentificationMapper.updateSpeciesIdentification(identification);

        // 异步调用AI识别
        asyncRecognizeAi(identificationId);

        return topicId;
    }

    /**
     * 构建社群话题内容
     */
    private String buildTopicContent(SpeciesIdentification identification)
    {
        StringBuilder content = new StringBuilder();
        content.append("【物种鉴定求助】\n\n");

        if (StringUtils.isNotEmpty(identification.getDescription())) {
            content.append("**详细描述：**\n");
            content.append(identification.getDescription()).append("\n\n");
        }

        if (StringUtils.isNotEmpty(identification.getFeatures())) {
            content.append("**特征描述：**\n");
            content.append(identification.getFeatures()).append("\n\n");
        }

        if (StringUtils.isNotEmpty(identification.getHabitat())) {
            content.append("**生境描述：**\n");
            content.append(identification.getHabitat()).append("\n\n");
        }

        if (StringUtils.isNotEmpty(identification.getLocation())) {
            content.append("**观察地点：**\n");
            content.append(identification.getLocation()).append("\n\n");
        }

        if (identification.getObservationTime() != null) {
            content.append("**观察时间：**\n");
            content.append(identification.getObservationTime()).append("\n\n");
        }

        content.append("---\n");
        content.append("*来自物种鉴定求助模块*");

        return content.toString();
    }

    /**
     * 异步AI识别（分享到社群时触发）
     *
     * @param identificationId 鉴定ID
     */
    @Override
    @Async
    public void asyncRecognizeAi(Long identificationId)
    {
        try {
            SpeciesIdentification identification = speciesIdentificationMapper.selectSpeciesIdentificationByIdentificationId(identificationId);
            if (identification == null) {
                log.warn("鉴定记录不存在，ID: {}", identificationId);
                return;
            }

            if (StringUtils.isEmpty(identification.getImages())) {
                log.warn("鉴定记录没有图片，无法AI识别，ID: {}", identificationId);
                return;
            }

            // 获取第一张图片URL（七牛云URL）
            String[] imageArray = identification.getImages().split(",");
            String imageUrl = imageArray[0].trim();

            // 优先使用通用识别，失败则尝试动物/植物识别
            JSONObject result = baiduAiUtil.generalRecognition(null, imageUrl);

            if (result != null && result.containsKey("result")) {
                JSONArray results = result.getJSONArray("result");
                if (results != null && !results.isEmpty()) {
                    JSONObject topResult = results.getJSONObject(0);
                    String speciesName = topResult.getString("keyword");
                    Double score = topResult.getDouble("score");

                    // 获取百度百科信息
                    String baikeUrl = null;
                    String baikeImageUrl = null;
                    String baikeDescription = null;

                    if (topResult.containsKey("baike_info")) {
                        JSONObject baikeInfo = topResult.getJSONObject("baike_info");
                        baikeUrl = baikeInfo.getString("baike_url");
                        baikeImageUrl = baikeInfo.getString("image_url");
                        baikeDescription = baikeInfo.getString("description");
                    }

                    // 更新鉴定记录
                    identification.setAiSpeciesName(speciesName);
                    identification.setAiScore(score);
                    identification.setAiType("general");
                    identification.setBaikeUrl(baikeUrl);
                    identification.setBaikeImageUrl(baikeImageUrl);
                    identification.setBaikeDescription(baikeDescription);
                    identification.setUpdateTime(new Date());

                    speciesIdentificationMapper.updateSpeciesIdentification(identification);

                    log.info("AI识别成功，鉴定ID: {}, 物种: {}, 置信度: {}", identificationId, speciesName, score);
                }
            }
        } catch (Exception e) {
            log.error("AI识别失败，鉴定ID: {}", identificationId, e);
        }
    }

    /**
     * 提交投票
     *
     * @param identificationId 鉴定ID
     * @param userId 用户ID
     * @param userName 用户名
     * @param voteType 投票类型：agree/disagree
     * @return 结果
     */
    @Override
    @Transactional
    public int submitVote(Long identificationId, Long userId, String userName, String voteType)
    {
        // 检查鉴定记录是否存在
        SpeciesIdentification identification = speciesIdentificationMapper.selectSpeciesIdentificationByIdentificationId(identificationId);
        if (identification == null) {
            throw new RuntimeException("鉴定记录不存在");
        }

        // 检查是否已分享（只有分享到社群的记录才能投票）
        if (!Integer.valueOf(1).equals(identification.getIsShared())) {
            throw new RuntimeException("该鉴定尚未分享到社群，无法投票");
        }

        // 检查投票状态
        if (!"1".equals(identification.getVoteStatus())) {
            throw new RuntimeException("投票已结束，无法继续投票");
        }

        // 检查用户是否已投票
        IdentificationVote existingVote = voteService.selectByUserAndId(userId, identificationId);
        if (existingVote != null) {
            throw new RuntimeException("您已经投过票了");
        }

        // 获取用户类型确定权重
        SysUser user = userMapper.selectUserById(userId);
        String userType = user != null ? user.getUserType() : "1";

        // 严格规则：鉴定者(userType=2)权重5，探索者(userType=1)权重1
        int weight = "2".equals(userType) ? 5 : 1;

        // 提交投票
        int result = voteService.submitVote(identificationId, userId, userName, userType, voteType);

        // 更新投票积分
        List<IdentificationVote> votes = voteService.selectByIdentificationId(identificationId);
        int agreeScore = 0;
        int disagreeScore = 0;

        for (IdentificationVote vote : votes) {
            if ("0".equals(vote.getVoteType())) {
                agreeScore += vote.getVoteWeight();
            } else {
                disagreeScore += vote.getVoteWeight();
            }
        }

        // 更新鉴定记录的积分
        speciesIdentificationMapper.updateVoteResult(
                identificationId,
                identification.getVoteStatus(),
                identification.getVoteResult(),
                agreeScore,
                disagreeScore
        );

        // 检查是否达到20分阈值
        if (agreeScore >= 20) {
            // 自动结束投票，结果为同意
            voteMapper.endVoting(identificationId, "0");
            speciesIdentificationMapper.updateStatus(identificationId, 2); // 2=已鉴定
            log.info("投票自动结束，鉴定ID: {}, 结果: 同意(积分: {})", identificationId, agreeScore);
        } else if (disagreeScore >= 20) {
            // 自动结束投票，结果为不同意
            voteMapper.endVoting(identificationId, "1");
            speciesIdentificationMapper.updateStatus(identificationId, 2); // 2=已鉴定
            log.info("投票自动结束，鉴定ID: {}, 结果: 不同意(积分: {})", identificationId, disagreeScore);
        }

        return result;
    }

    /**
     * 获取投票状态信息
     *
     * @param identificationId 鉴定ID
     * @return 投票状态对象
     */
    @Override
    public VoteStatusInfo getVoteStatus(Long identificationId)
    {
        SpeciesIdentification identification = speciesIdentificationMapper.selectSpeciesIdentificationByIdentificationId(identificationId);
        if (identification == null) {
            return null;
        }

        int agreeScore = identification.getVoteAgreeScore() != null ? identification.getVoteAgreeScore() : 0;
        int disagreeScore = identification.getVoteDisagreeScore() != null ? identification.getVoteDisagreeScore() : 0;
        String voteStatus = identification.getVoteStatus() != null ? identification.getVoteStatus() : "0";
        String voteResult = identification.getVoteResult() != null ? identification.getVoteResult() : "2";

        return new VoteStatusInfo(agreeScore, disagreeScore, voteStatus, voteResult);
    }

    /**
     * 结束投票
     *
     * @param identificationId 鉴定ID
     * @return 结果
     */
    @Override
    @Transactional
    public int endVoting(Long identificationId)
    {
        SpeciesIdentification identification = speciesIdentificationMapper.selectSpeciesIdentificationByIdentificationId(identificationId);
        if (identification == null) {
            throw new RuntimeException("鉴定记录不存在");
        }

        if (!"1".equals(identification.getVoteStatus())) {
            throw new RuntimeException("投票已结束");
        }

        List<IdentificationVote> votes = voteService.selectByIdentificationId(identificationId);
        int agreeScore = 0;
        int disagreeScore = 0;

        for (IdentificationVote vote : votes) {
            if ("0".equals(vote.getVoteType())) {
                agreeScore += vote.getVoteWeight();
            } else {
                disagreeScore += vote.getVoteWeight();
            }
        }

        String voteResult;
        if (agreeScore >= 20) {
            voteResult = "0"; // 0=同意
        } else if (disagreeScore >= 20) {
            voteResult = "1"; // 1=不同意
        } else if (agreeScore > disagreeScore) {
            voteResult = "0"; // 0=同意
        } else if (disagreeScore > agreeScore) {
            voteResult = "1"; // 1=不同意
        } else {
            voteResult = "2"; // 2=未确定
        }

        voteMapper.endVoting(identificationId, voteResult);
        speciesIdentificationMapper.updateStatus(identificationId, 2); // 2=已鉴定

        return 1;
    }

    /**
     * 获取投票详情列表
     *
     * @param identificationId 鉴定ID
     * @return 投票记录列表
     */
    @Override
    public List<IdentificationVote> getVoteDetails(Long identificationId)
    {
        return voteService.selectByIdentificationId(identificationId);
    }

    /**
     * 获取投票数量
     *
     * @param identificationId 鉴定ID
     * @return 投票数量
     */
    @Override
    public int getVoteCount(Long identificationId)
    {
        return voteService.countByIdentificationId(identificationId);
    }
}
