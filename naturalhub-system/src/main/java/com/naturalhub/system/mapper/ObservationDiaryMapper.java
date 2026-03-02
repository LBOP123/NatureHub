package com.naturalhub.system.mapper;

import java.util.List;
import com.naturalhub.system.domain.ObservationDiary;

/**
 * 个人观察日志Mapper接口
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public interface ObservationDiaryMapper 
{
    /**
     * 查询个人观察日志
     * 
     * @param diaryId 个人观察日志主键
     * @return 个人观察日志
     */
    public ObservationDiary selectObservationDiaryByDiaryId(Long diaryId);

    /**
     * 查询个人观察日志列表
     * 
     * @param observationDiary 个人观察日志
     * @return 个人观察日志集合
     */
    public List<ObservationDiary> selectObservationDiaryList(ObservationDiary observationDiary);

    /**
     * 新增个人观察日志
     * 
     * @param observationDiary 个人观察日志
     * @return 结果
     */
    public int insertObservationDiary(ObservationDiary observationDiary);

    /**
     * 修改个人观察日志
     * 
     * @param observationDiary 个人观察日志
     * @return 结果
     */
    public int updateObservationDiary(ObservationDiary observationDiary);

    /**
     * 删除个人观察日志
     * 
     * @param diaryId 个人观察日志主键
     * @return 结果
     */
    public int deleteObservationDiaryByDiaryId(Long diaryId);

    /**
     * 批量删除个人观察日志
     * 
     * @param diaryIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteObservationDiaryByDiaryIds(Long[] diaryIds);
}
