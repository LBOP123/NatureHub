package com.naturalhub.system.mapper;

import java.util.List;
import com.naturalhub.system.domain.CommunityReport;

/**
 * 社群举报Mapper接口
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public interface CommunityReportMapper 
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
     * 删除社群举报
     * 
     * @param reportId 社群举报主键
     * @return 结果
     */
    public int deleteCommunityReportByReportId(Long reportId);

    /**
     * 批量删除社群举报
     * 
     * @param reportIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCommunityReportByReportIds(Long[] reportIds);
}
