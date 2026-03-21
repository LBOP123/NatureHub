package com.naturalhub.system.service;

import java.util.List;
import com.naturalhub.system.domain.ObservationRecord;

/**
 * 日志关联观察记录Service接口
 * 
 * @author naturalhub
 * @date 2025-03-21
 */
public interface IDiaryRecordRelationService {
    
    /**
     * 获取日志关联的观察记录列表
     * 
     * @param diaryId 日志ID
     * @return 观察记录列表
     */
    List<ObservationRecord> getRecordsByDiaryId(Long diaryId);
    
    /**
     * 关联观察记录
     * 
     * @param diaryId 日志ID
     * @param recordId 观察记录ID
     * @return 结果
     */
    int addRelation(Long diaryId, Long recordId);
    
    /**
     * 取消关联
     * 
     * @param diaryId 日志ID
     * @param recordId 观察记录ID
     * @return 结果
     */
    int removeRelation(Long diaryId, Long recordId);
    
    /**
     * 更新日志的关联记录（先删除再新增）
     * 
     * @param diaryId 日志ID
     * @param recordIds 观察记录ID列表
     * @return 结果
     */
    int updateRelations(Long diaryId, List<Long> recordIds);
    
    /**
     * 删除日志的所有关联
     * 
     * @param diaryId 日志ID
     * @return 结果
     */
    int deleteRelationsByDiaryId(Long diaryId);
}
