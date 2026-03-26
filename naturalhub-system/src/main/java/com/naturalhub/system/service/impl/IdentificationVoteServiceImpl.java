package com.naturalhub.system.service.impl;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.naturalhub.system.domain.IdentificationVote;
import com.naturalhub.system.mapper.IdentificationVoteMapper;
import com.naturalhub.system.service.IIdentificationVoteService;

/**
 * 物种鉴定投票Service业务层处理
 *
 * @author NaturalHub
 * @date 2026-03-21
 */
@Service
public class IdentificationVoteServiceImpl implements IIdentificationVoteService
{
    private static final Logger log = LoggerFactory.getLogger(IdentificationVoteServiceImpl.class);

    @Autowired
    private IdentificationVoteMapper identificationVoteMapper;

    /**
     * 查询投票记录列表
     *
     * @param vote 投票记录
     * @return 投票记录集合
     */
    @Override
    public List<IdentificationVote> selectIdentificationVoteList(IdentificationVote vote)
    {
        return identificationVoteMapper.selectIdentificationVoteList(vote);
    }

    /**
     * 根据鉴定ID查询投票列表
     *
     * @param identificationId 鉴定请求ID
     * @return 投票记录集合
     */
    @Override
    public List<IdentificationVote> selectByIdentificationId(Long identificationId)
    {
        return identificationVoteMapper.selectByIdentificationId(identificationId);
    }

    /**
     * 查询用户是否已投票
     *
     * @param userId 用户ID
     * @param identificationId 鉴定请求ID
     * @return 投票记录
     */
    @Override
    public IdentificationVote selectByUserAndId(Long userId, Long identificationId)
    {
        return identificationVoteMapper.selectByUserAndId(userId, identificationId);
    }

    /**
     * 统计投票数量
     *
     * @param identificationId 鉴定请求ID
     * @return 投票数量
     */
    @Override
    public int countByIdentificationId(Long identificationId)
    {
        return identificationVoteMapper.countByIdentificationId(identificationId);
    }

    /**
     * 提交投票
     *
     * @param identificationId 鉴定请求ID
     * @param userId 用户ID
     * @param userName 用户名
     * @param userType 用户类型
     * @param voteType 投票类型：0=同意, 1=不同意
     * @return 结果
     */
    @Override
    @Transactional
    public int submitVote(Long identificationId, Long userId, String userName, String userType, String voteType)
    {
        // 兼容旧值：agree -> 0, disagree -> 1
        if ("agree".equals(voteType)) {
            voteType = "0";
        } else if ("disagree".equals(voteType)) {
            voteType = "1";
        }
        if (!"0".equals(voteType) && !"1".equals(voteType)) {
            throw new RuntimeException("投票类型必须是 0(同意) 或 1(不同意)");
        }
        IdentificationVote vote = new IdentificationVote();
        vote.setIdentificationId(identificationId);
        vote.setUserId(userId);
        vote.setUserName(userName);
        vote.setUserType(userType);
        vote.setVoteType(voteType);
        
        // 严格规则：鉴定者(userType=2)权重5，探索者(userType=1)权重1
        int weight = "2".equals(userType) ? 5 : 1;
        vote.setVoteWeight(weight);
        vote.setVoteTime(new Date());
        
        return identificationVoteMapper.insertIdentificationVote(vote);
    }

    /**
     * 结束投票并判定结果
     *
     * @param identificationId 鉴定请求ID
     * @return 结果
     */
    @Override
    @Transactional
    public int endVoting(Long identificationId)
    {
        List<IdentificationVote> votes = selectByIdentificationId(identificationId);
        int agreeScore = 0;
        int disagreeScore = 0;

        for (IdentificationVote vote : votes) {
            if ("0".equals(vote.getVoteType())) {
                agreeScore += vote.getVoteWeight();
            } else if ("1".equals(vote.getVoteType())) {
                disagreeScore += vote.getVoteWeight();
            }
        }

        String voteResult;
        if (agreeScore >= 20) {
            voteResult = "0"; // 0=同意
        } else if (disagreeScore >= 20) {
            voteResult = "1"; // 1=不同意
        } else {
            voteResult = "2"; // 2=未确定
        }

        return identificationVoteMapper.endVoting(identificationId, voteResult);
    }

    /**
     * 批量插入投票记录
     *
     * @param votes 投票记录列表
     * @return 结果
     */
    @Override
    public int batchInsert(List<IdentificationVote> votes)
    {
        return identificationVoteMapper.batchInsertIdentificationVote(votes);
    }

    /**
     * 删除投票记录
     *
     * @param voteId 投票ID
     * @return 结果
     */
    @Override
    public int deleteIdentificationVote(Long voteId)
    {
        return identificationVoteMapper.deleteIdentificationVote(voteId);
    }

    /**
     * 批量删除投票记录
     *
     * @param voteIds 投票ID数组
     * @return 结果
     */
    @Override
    public int deleteIdentificationVoteByIds(Long[] voteIds)
    {
        return identificationVoteMapper.deleteIdentificationVoteByIds(voteIds);
    }
}
