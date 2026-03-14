package com.naturalhub.system.service;

import java.util.List;
import com.naturalhub.system.domain.ObservationRecord;

/**
 * 观察记录Service接口
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
public interface IObservationRecordService 
{
    /**
     * 查询观察记录
     * 
     * @param recordId 观察记录主键
     * @return 观察记录
     */
    public ObservationRecord selectObservationRecordByRecordId(Long recordId);

    /**
     * 查询观察记录列表
     * 
     * @param observationRecord 观察记录
     * @return 观察记录集合
     */
    public List<ObservationRecord> selectObservationRecordList(ObservationRecord observationRecord);

    /**
     * 新增观察记录
     * 
     * @param observationRecord 观察记录
     * @return 结果
     */
    public int insertObservationRecord(ObservationRecord observationRecord);

    /**
     * 修改观察记录
     * 
     * @param observationRecord 观察记录
     * @return 结果
     */
    public int updateObservationRecord(ObservationRecord observationRecord);

    /**
     * 批量删除观察记录
     * 
     * @param recordIds 需要删除的观察记录主键集合
     * @return 结果
     */
    public int deleteObservationRecordByRecordIds(Long[] recordIds);

    /**
     * 删除观察记录信息
     * 
     * @param recordId 观察记录主键
     * @return 结果
     */
    public int deleteObservationRecordByRecordId(Long recordId);

    /**
     * 提交审核
     * 
     * @param recordId 观察记录主键
     * @return 结果
     */
    public int submitForReview(Long recordId);

    /**
     * 根据分享话题ID查询观察记录（兼容历史数据）
     * @param sharedTopicId 话题ID
     * @return 观察记录
     */
    public ObservationRecord selectObservationRecordBySharedTopicId(Long sharedTopicId);

    /**
     * 分享到社群
     * 
     * @param recordId 观察记录主键
     * @return 社群话题ID
     */
    public Long shareToCommunity(Long recordId);
}
