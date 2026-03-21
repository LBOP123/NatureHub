package com.naturalhub.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.naturalhub.system.domain.DiaryRecordRelation;
import com.naturalhub.system.domain.ObservationDiary;
import com.naturalhub.system.domain.ObservationRecord;
import com.naturalhub.system.mapper.DiaryRecordRelationMapper;
import com.naturalhub.system.mapper.ObservationDiaryMapper;
import com.naturalhub.system.service.IDiaryRecordRelationService;
import com.naturalhub.common.utils.DateUtils;

/**
 * 日志关联观察记录Service业务层处理
 * 
 * @author naturalhub
 * @date 2025-03-21
 */
@Service
public class DiaryRecordRelationServiceImpl implements IDiaryRecordRelationService {
    
    @Autowired
    private DiaryRecordRelationMapper diaryRecordRelationMapper;

    @Autowired
    private ObservationDiaryMapper observationDiaryMapper;
    
    /**
     * 获取日志关联的观察记录列表
     * 
     * @param diaryId 日志ID
     * @return 观察记录列表
     */
    @Override
    public List<ObservationRecord> getRecordsByDiaryId(Long diaryId) {
        return diaryRecordRelationMapper.selectRecordsByDiaryId(diaryId);
    }
    
    /**
     * 关联观察记录
     * 
     * @param diaryId 日志ID
     * @param recordId 观察记录ID
     * @return 结果
     */
    @Override
    public int addRelation(Long diaryId, Long recordId) {
        DiaryRecordRelation relation = new DiaryRecordRelation();
        relation.setDiaryId(diaryId);
        relation.setRecordId(recordId);
        relation.setCreateTime(DateUtils.getNowDate());
        return diaryRecordRelationMapper.insertRelation(relation);
    }
    
    /**
     * 取消关联
     * 
     * @param diaryId 日志ID
     * @param recordId 观察记录ID
     * @return 结果
     */
    @Override
    public int removeRelation(Long diaryId, Long recordId) {
        return diaryRecordRelationMapper.deleteRelation(diaryId, recordId);
    }
    
    /**
     * 更新日志的关联记录（先删除再新增）
     * 
     * @param diaryId 日志ID
     * @param recordIds 观察记录ID列表
     * @return 结果
     */
    @Override
    public int updateRelations(Long diaryId, List<Long> recordIds) {
        // 先删除所有关联
        diaryRecordRelationMapper.deleteRelationsByDiaryId(diaryId);
        
        // 再新增新的关联
        int result = 1;
        if (recordIds != null && !recordIds.isEmpty()) {
            result = diaryRecordRelationMapper.batchInsertRelations(diaryId, recordIds);
            
            // 更新日志的记录计数
            ObservationDiary diary = new ObservationDiary();
            diary.setDiaryId(diaryId);
            diary.setRecordCount(recordIds.size());
            observationDiaryMapper.updateRecordCount(diary);
        } else {
            // 如果没有关联记录，设置计数为 0
            ObservationDiary diary = new ObservationDiary();
            diary.setDiaryId(diaryId);
            diary.setRecordCount(0);
            observationDiaryMapper.updateRecordCount(diary);
        }
        
        return result;
    }
    
    /**
     * 删除日志的所有关联
     * 
     * @param diaryId 日志ID
     * @return 结果
     */
    @Override
    public int deleteRelationsByDiaryId(Long diaryId) {
        return diaryRecordRelationMapper.deleteRelationsByDiaryId(diaryId);
    }
}
