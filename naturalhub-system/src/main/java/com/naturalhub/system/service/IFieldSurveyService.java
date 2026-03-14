package com.naturalhub.system.service;

import java.util.List;
import com.naturalhub.system.domain.FieldSurvey;

/**
 * 野外调查记录Service接口
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
public interface IFieldSurveyService 
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
     * 批量删除野外调查记录
     * 
     * @param surveyIds 需要删除的野外调查记录主键集合
     * @return 结果
     */
    public int deleteFieldSurveyBySurveyIds(Long[] surveyIds);

    /**
     * 删除野外调查记录信息
     * 
     * @param surveyId 野外调查记录主键
     * @return 结果
     */
    public int deleteFieldSurveyBySurveyId(Long surveyId);

    /**
     * 审核野外调查记录
     * 
     * @param surveyId 调查ID
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     * @return 结果
     */
    public int audit(Long surveyId, Integer auditStatus, String auditRemark);

    /**
     * 根据分享话题ID查询野外调查记录（兼容历史数据）
     * @param sharedTopicId 话题ID
     * @return 野外调查记录
     */
    public FieldSurvey selectFieldSurveyBySharedTopicId(Long sharedTopicId);

    /**
     * 分享到社群
     * 
     * @param surveyId 调查ID
     * @return 社群话题ID
     */
    public Long shareToCommunity(Long surveyId);
}
