package com.naturalhub.system.mapper;

import com.naturalhub.system.domain.IdentificationVote;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 物种鉴定投票Mapper接口
 *
 * @author NaturalHub
 * @date 2026-03-21
 */
public interface IdentificationVoteMapper
{
    /**
     * 查询投票记录列表
     *
     * @param vote 投票记录
     * @return 投票记录集合
     */
    public List<IdentificationVote> selectIdentificationVoteList(IdentificationVote vote);

    /**
     * 根据鉴定ID查询投票列表
     *
     * @param identificationId 鉴定请求ID
     * @return 投票记录集合
     */
    public List<IdentificationVote> selectByIdentificationId(Long identificationId);

    /**
     * 查询用户是否已投票
     *
     * @param userId 用户ID
     * @param identificationId 鉴定请求ID
     * @return 投票记录
     */
    public IdentificationVote selectByUserAndId(@Param("userId") Long userId, @Param("identificationId") Long identificationId);

    /**
     * 统计投票数量
     *
     * @param identificationId 鉴定请求ID
     * @return 投票数量
     */
    public int countByIdentificationId(Long identificationId);

    /**
     * 插入投票记录
     *
     * @param vote 投票记录
     * @return 结果
     */
    public int insertIdentificationVote(IdentificationVote vote);

    /**
     * 批量插入投票记录
     *
     * @param votes 投票记录列表
     * @return 结果
     */
    public int batchInsertIdentificationVote(List<IdentificationVote> votes);
    /**
     * 删除投票记录
     *
     * @param voteId 投票ID
     * @return 结果
     */
    public int deleteIdentificationVote(Long voteId);
    /**
     * 批量删除投票记录
     *
     * @param voteIds 投票ID数组
     * @return 结果
     */
    public int deleteIdentificationVoteByIds(Long[] voteIds);

    /**
     * 结束投票并更新鉴定表
     *
     * @param identificationId 鉴定ID
     * @param voteResult 投票结果
     * @return 结果
     */
    public int endVoting(@Param("identificationId") Long identificationId, @Param("voteResult") String voteResult);
}
