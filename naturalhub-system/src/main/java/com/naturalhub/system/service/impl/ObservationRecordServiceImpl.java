package com.naturalhub.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.naturalhub.system.mapper.ObservationRecordMapper;
import com.naturalhub.system.domain.ObservationRecord;
import com.naturalhub.system.service.IObservationRecordService;

/**
 * 观察记录Service业务层处理
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
@Service
public class ObservationRecordServiceImpl implements IObservationRecordService 
{
    @Autowired
    private ObservationRecordMapper observationRecordMapper;

    /**
     * 查询观察记录
     * 
     * @param recordId 观察记录主键
     * @return 观察记录
     */
    @Override
    public ObservationRecord selectObservationRecordByRecordId(Long recordId)
    {
        return observationRecordMapper.selectObservationRecordByRecordId(recordId);
    }

    /**
     * 查询观察记录列表
     * 
     * @param observationRecord 观察记录
     * @return 观察记录
     */
    @Override
    public List<ObservationRecord> selectObservationRecordList(ObservationRecord observationRecord)
    {
        return observationRecordMapper.selectObservationRecordList(observationRecord);
    }

    /**
     * 新增观察记录
     * 
     * @param observationRecord 观察记录
     * @return 结果
     */
    @Override
    public int insertObservationRecord(ObservationRecord observationRecord)
    {
        // 设置默认状态为草稿
        if (observationRecord.getReviewStatus() == null) {
            observationRecord.setReviewStatus("draft");
        }
        // 设置默认删除标志
        if (observationRecord.getDelFlag() == null) {
            observationRecord.setDelFlag("0");
        }
        // 设置默认分享状态
        if (observationRecord.getIsShared() == null) {
            observationRecord.setIsShared("0");
        }
        return observationRecordMapper.insertObservationRecord(observationRecord);
    }

    /**
     * 修改观察记录
     * 
     * @param observationRecord 观察记录
     * @return 结果
     */
    @Override
    public int updateObservationRecord(ObservationRecord observationRecord)
    {
        return observationRecordMapper.updateObservationRecord(observationRecord);
    }

    /**
     * 批量删除观察记录
     * 
     * @param recordIds 需要删除的观察记录主键
     * @return 结果
     */
    @Override
    public int deleteObservationRecordByRecordIds(Long[] recordIds)
    {
        return observationRecordMapper.deleteObservationRecordByRecordIds(recordIds);
    }

    /**
     * 删除观察记录信息
     * 
     * @param recordId 观察记录主键
     * @return 结果
     */
    @Override
    public int deleteObservationRecordByRecordId(Long recordId)
    {
        return observationRecordMapper.deleteObservationRecordByRecordId(recordId);
    }

    /**
     * 提交审核
     * 
     * @param recordId 观察记录主键
     * @return 结果
     */
    @Override
    public int submitForReview(Long recordId)
    {
        return observationRecordMapper.submitForReview(recordId);
    }
}
