package com.naturalhub.system.mapper;

import java.util.List;
import com.naturalhub.system.domain.FieldSurvey;

/**
 * 野外调查记录Mapper接口
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
public interface FieldSurveyMapper 
{
    /**
     * 查询野外调查记录
     * 
     * @param surveyId 野外调查记录主键
     * @return 野外调查记录
     */
    public FieldSurvey selectFieldSurveyBySurveyId(Long surveyId);

    /**
     * 查询野外调查记录列表
     * 
     * @param fieldSurvey 野外调查记录
     * @return 野外调查记录集合
     */
    public List<FieldSurvey> selectFieldSurveyList(FieldSurvey fieldSurvey);

    /**
     * 新增野外调查记录
     * 
     * @param fieldSurvey 野外调查记录
     * @return 结果
     */
    public int insertFieldSurvey(FieldSurvey fieldSurvey);

    /**
     * 修改野外调查记录
     * 
     * @param fieldSurvey 野外调查记录
     * @return 结果
     */
    public int updateFieldSurvey(FieldSurvey fieldSurvey);

    /**
     * 删除野外调查记录
     * 
     * @param surveyId 野外调查记录主键
     * @return 结果
     */
    public int deleteFieldSurveyBySurveyId(Long surveyId);

    /**
     * 批量删除野外调查记录
     * 
     * @param surveyIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFieldSurveyBySurveyIds(Long[] surveyIds);

    /**
     * 根据分享话题ID查询野外调查记录
     * 
     * @param sharedTopicId 分享话题ID
     * @return 野外调查记录
     */
    public FieldSurvey selectFieldSurveyBySharedTopicId(Long sharedTopicId);
}
