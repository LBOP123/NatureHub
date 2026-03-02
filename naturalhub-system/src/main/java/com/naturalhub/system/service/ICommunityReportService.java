package com.naturalhub.system.service;

import java.util.List;
import com.naturalhub.system.domain.CommunityReport;

/**
 * 社群举报Service接口
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public interface ICommunityReportService 
{
    /**
     * 查询社群举报
     * 
     * @param reportId 社群举报主键
     * @return 社群举报
     */
    public CommunityReport selectCommunityReportByReportId(Long reportId);

    /**
     * 查询社群举报列表
     * 
     * @param communityReport 社群举报
     * @return 社群举报集合
     */
    public List<CommunityReport> selectCommunityReportList(CommunityReport communityReport);

    /**
     * 新增社群举报
     * 
     * @param communityReport 社群举报
     * @return 结果
     */
    public int insertCommunityReport(CommunityReport communityReport);

    /**
     * 修改社群举报
     * 
     * @param communityReport 社群举报
     * @return 结果
     */
    public int updateCommunityReport(CommunityReport communityReport);

    /**
     * 批量删除社群举报
     * 
     * @param reportIds 需要删除的社群举报主键集合
     * @return 结果
     */
    public int deleteCommunityReportByReportIds(Long[] reportIds);

    /**
     * 删除社群举报信息
     * 
     * @param reportId 社群举报主键
     * @return 结果
     */
    public int deleteCommunityReportByReportId(Long reportId);

    /**
     * 处理举报
     * 
     * @param reportId 举报ID
     * @param handleResult 处理结果
     * @param handleRemark 处理备注
     * @return 结果
     */
    public int handleReport(Long reportId, String handleResult, String handleRemark);
}
