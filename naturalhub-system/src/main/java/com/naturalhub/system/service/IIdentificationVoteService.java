package com.naturalhub.system.service;

import com.naturalhub.system.domain.IdentificationVote;

import java.util.List;

/**
 * 物种鉴定投票Service接口
 *
 * @author NaturalHub
 * @date 2026-03-21
 */
public interface IIdentificationVoteService
{
    /**
     * 查询投票记录列表
     *
     * @param vote 投票记录
     * @return 投票记录集合
     */
    List<IdentificationVote> selectIdentificationVoteList(IdentificationVote vote);

    /**
     * 根据鉴定ID查询投票列表
     *
     * @param identificationId 鉴定请求ID
     * @return 投票记录集合
     */
    List<IdentificationVote> selectByIdentificationId(Long identificationId);

    /**
     * 查询用户是否已投票
     *
     * @param userId 用户ID
     * @param identificationId 鉴定请求ID
     * @return 投票记录
     */
    IdentificationVote selectByUserAndId(Long userId, Long identificationId);

    /**
     * 统计投票数量
     *
     * @param identificationId 鉴定请求ID
     * @return 投票数量
     */
    int countByIdentificationId(Long identificationId);

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
    int submitVote(Long identificationId, Long userId, String userName, String userType, String voteType);

    /**
     * 结束投票并判定结果
     *
     * @param identificationId 鉴定请求ID
     * @return 结果
     */
    int endVoting(Long identificationId);

    /**
     * 批量插入投票记录
     *
     * @param votes 投票记录列表
     * @return 结果
     */
    int batchInsert(List<IdentificationVote> votes);

    /**
     * 删除投票记录
     *
     * @param voteId 投票ID
     * @return 结果
     */
    int deleteIdentificationVote(Long voteId);

    /**
     * 批量删除投票记录
     *
     * @param voteIds 投票ID数组
     * @return 结果
     */
    int deleteIdentificationVoteByIds(Long[] voteIds);
}
