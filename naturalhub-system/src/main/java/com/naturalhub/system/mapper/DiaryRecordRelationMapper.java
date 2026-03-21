package com.naturalhub.system.mapper;

import java.util.List;
import com.naturalhub.system.domain.DiaryRecordRelation;
import com.naturalhub.system.domain.ObservationRecord;
import org.apache.ibatis.annotations.Param;

/**
 * 日志关联观察记录Mapper接口
 * 
 * @author naturalhub
 * @date 2025-03-21
 */
public interface DiaryRecordRelationMapper {
    
    /**
     * 查询日志关联的所有观察记录ID
     * 
     * @param diaryId 日志ID
     * @return 观察记录ID列表
     */
    List<Long> selectRecordIdsByDiaryId(Long diaryId);
    
    /**
     * 查询日志关联的所有观察记录
     * 
     * @param diaryId 日志ID
     * @return 观察记录列表
     */
    List<ObservationRecord> selectRecordsByDiaryId(Long diaryId);
    
    /**
     * 新增关联
     * 
     * @param relation 关联对象
     * @return 结果
     */
    int insertRelation(DiaryRecordRelation relation);
    
    /**
     * 删除关联
     * 
     * @param diaryId 日志ID
     * @param recordId 观察记录ID
     * @return 结果
     */
    int deleteRelation(@Param("diaryId") Long diaryId, @Param("recordId") Long recordId);
    
    /**
     * 删除日志的所有关联
     * 
     * @param diaryId 日志ID
     * @return 结果
     */
    int deleteRelationsByDiaryId(Long diaryId);
    
    /**
     * 批量新增关联
     * 
     * @param diaryId 日志ID
     * @param recordIds 观察记录ID列表
     * @return 结果
     */
    int batchInsertRelations(@Param("diaryId") Long diaryId, @Param("recordIds") List<Long> recordIds);
}
