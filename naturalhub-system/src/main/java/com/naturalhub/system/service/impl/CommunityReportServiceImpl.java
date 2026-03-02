package com.naturalhub.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.system.mapper.CommunityReportMapper;
import com.naturalhub.system.mapper.CommunityTopicMapper;
import com.naturalhub.system.mapper.CommunityCommentMapper;
import com.naturalhub.system.domain.CommunityReport;
import com.naturalhub.system.service.ICommunityReportService;

/**
 * 社群举报Service业务层处理
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
@Service
public class CommunityReportServiceImpl implements ICommunityReportService 
{
    @Autowired
    private CommunityReportMapper communityReportMapper;

    @Autowired
    private CommunityTopicMapper communityTopicMapper;

    @Autowired
    private CommunityCommentMapper communityCommentMapper;

    /**
     * 查询社群举报
     * 
     * @param reportId 社群举报主键
     * @return 社群举报
     */
    @Override
    public CommunityReport selectCommunityReportByReportId(Long reportId)
    {
        return communityReportMapper.selectCommunityReportByReportId(reportId);
    }

    /**
     * 查询社群举报列表
     * 
     * @param communityReport 社群举报
     * @return 社群举报
     */
    @Override
    public List<CommunityReport> selectCommunityReportList(CommunityReport communityReport)
    {
        return communityReportMapper.selectCommunityReportList(communityReport);
    }

    /**
     * 新增社群举报
     * 
     * @param communityReport 社群举报
     * @return 结果
     */
    @Override
    public int insertCommunityReport(CommunityReport communityReport)
    {
        communityReport.setStatus("pending");
        // 获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        // 设置举报人ID
        communityReport.setReporterId(userId);
        communityReport.setReporterName(username);
        return communityReportMapper.insertCommunityReport(communityReport);
    }

    /**
     * 修改社群举报
     * 
     * @param communityReport 社群举报
     * @return 结果
     */
    @Override
    public int updateCommunityReport(CommunityReport communityReport)
    {
        return communityReportMapper.updateCommunityReport(communityReport);
    }

    /**
     * 批量删除社群举报
     * 
     * @param reportIds 需要删除的社群举报主键
     * @return 结果
     */
    @Override
    public int deleteCommunityReportByReportIds(Long[] reportIds)
    {
        return communityReportMapper.deleteCommunityReportByReportIds(reportIds);
    }

    /**
     * 删除社群举报信息
     * 
     * @param reportId 社群举报主键
     * @return 结果
     */
    @Override
    public int deleteCommunityReportByReportId(Long reportId)
    {
        return communityReportMapper.deleteCommunityReportByReportId(reportId);
    }

    /**
     * 处理举报
     * 
     * @param reportId 举报ID
     * @param handleResult 处理结果
     * @param handleRemark 处理备注
     * @return 结果
     */
    @Override
    public int handleReport(Long reportId, String handleResult, String handleRemark)
    {
        CommunityReport report = communityReportMapper.selectCommunityReportByReportId(reportId);
        if (report == null)
        {
            return 0;
        }

        // 根据处理结果执行相应操作
        if ("delete_content".equals(handleResult))
        {
            // 删除内容
            if ("topic".equals(report.getReportType()))
            {
                communityTopicMapper.deleteCommunityTopicByTopicId(report.getTargetId());
            }
            else if ("comment".equals(report.getReportType()))
            {
                communityCommentMapper.deleteCommunityCommentByCommentId(report.getTargetId());
            }
        }

        // 更新举报记录
        report.setStatus("processed");
        report.setHandleResult(handleResult);
        report.setHandleRemark(handleRemark);
        report.setHandleTime(new Date());
        report.setHandleBy(SecurityUtils.getUsername());

        return communityReportMapper.updateCommunityReport(report);
    }
}
