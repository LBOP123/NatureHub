package com.naturalhub.system.service.impl;

import java.util.List;
import com.naturalhub.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.naturalhub.system.mapper.ObservationDiaryMapper;
import com.naturalhub.system.domain.ObservationDiary;
import com.naturalhub.system.service.IObservationDiaryService;

/**
 * 个人观察日志Service业务层处理
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
@Service
public class ObservationDiaryServiceImpl implements IObservationDiaryService 
{
    @Autowired
    private ObservationDiaryMapper observationDiaryMapper;

    /**
     * 查询个人观察日志
     * 
     * @param diaryId 个人观察日志主键
     * @return 个人观察日志
     */
    @Override
    public ObservationDiary selectObservationDiaryByDiaryId(Long diaryId)
    {
        return observationDiaryMapper.selectObservationDiaryByDiaryId(diaryId);
    }

    /**
     * 查询个人观察日志列表
     * 
     * @param observationDiary 个人观察日志
     * @return 个人观察日志
     */
    @Override
    public List<ObservationDiary> selectObservationDiaryList(ObservationDiary observationDiary)
    {
        return observationDiaryMapper.selectObservationDiaryList(observationDiary);
    }

    /**
     * 新增个人观察日志
     * 
     * @param observationDiary 个人观察日志
     * @return 结果
     */
    @Override
    public int insertObservationDiary(ObservationDiary observationDiary)
    {
        observationDiary.setCreateTime(DateUtils.getNowDate());
        return observationDiaryMapper.insertObservationDiary(observationDiary);
    }

    /**
     * 修改个人观察日志
     * 
     * @param observationDiary 个人观察日志
     * @return 结果
     */
    @Override
    public int updateObservationDiary(ObservationDiary observationDiary)
    {
        observationDiary.setUpdateTime(DateUtils.getNowDate());
        return observationDiaryMapper.updateObservationDiary(observationDiary);
    }

    /**
     * 批量删除个人观察日志
     * 
     * @param diaryIds 需要删除的个人观察日志主键
     * @return 结果
     */
    @Override
    public int deleteObservationDiaryByDiaryIds(Long[] diaryIds)
    {
        return observationDiaryMapper.deleteObservationDiaryByDiaryIds(diaryIds);
    }

    /**
     * 删除个人观察日志信息
     * 
     * @param diaryId 个人观察日志主键
     * @return 结果
     */
    @Override
    public int deleteObservationDiaryByDiaryId(Long diaryId)
    {
        return observationDiaryMapper.deleteObservationDiaryByDiaryId(diaryId);
    }
}
