package com.naturalhub.web.controller.community;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.naturalhub.common.annotation.Log;
import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.enums.BusinessType;
import com.naturalhub.system.domain.CommunityReport;
import com.naturalhub.system.service.ICommunityReportService;
import com.naturalhub.common.utils.poi.ExcelUtil;
import com.naturalhub.common.core.page.TableDataInfo;

/**
 * 社群举报管理Controller
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
@RestController
@RequestMapping("/community/report")
public class CommunityReportController extends BaseController
{
    @Autowired
    private ICommunityReportService communityReportService;

    /**
     * 查询社群举报列表
     */
    @PreAuthorize("@ss.hasPermi('community:report:list')")
    @GetMapping("/list")
    public TableDataInfo list(CommunityReport communityReport)
    {
        startPage();
        List<CommunityReport> list = communityReportService.selectCommunityReportList(communityReport);
        return getDataTable(list);
    }

    /**
     * 导出社群举报列表
     */
    @PreAuthorize("@ss.hasPermi('community:report:export')")
    @Log(title = "社群举报", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CommunityReport communityReport)
    {
        List<CommunityReport> list = communityReportService.selectCommunityReportList(communityReport);
        ExcelUtil<CommunityReport> util = new ExcelUtil<CommunityReport>(CommunityReport.class);
        util.exportExcel(response, list, "社群举报数据");
    }

    /**
     * 获取社群举报详细信息
     */
    @PreAuthorize("@ss.hasPermi('community:report:query')")
    @GetMapping(value = "/{reportId}")
    public AjaxResult getInfo(@PathVariable("reportId") Long reportId)
    {
        return success(communityReportService.selectCommunityReportByReportId(reportId));
    }

    /**
     * 新增社群举报
     */
    @PreAuthorize("@ss.hasPermi('community:report:add')")
    @Log(title = "社群举报", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CommunityReport communityReport)
    {
        return toAjax(communityReportService.insertCommunityReport(communityReport));
    }

    /**
     * 修改社群举报
     */
    @PreAuthorize("@ss.hasPermi('community:report:edit')")
    @Log(title = "社群举报", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CommunityReport communityReport)
    {
        return toAjax(communityReportService.updateCommunityReport(communityReport));
    }

    /**
     * 删除社群举报
     */
    @PreAuthorize("@ss.hasPermi('community:report:remove')")
    @Log(title = "社群举报", businessType = BusinessType.DELETE)
	@DeleteMapping("/{reportIds}")
    public AjaxResult remove(@PathVariable Long[] reportIds)
    {
        return toAjax(communityReportService.deleteCommunityReportByReportIds(reportIds));
    }

    /**
     * 处理举报
     */
    @PreAuthorize("@ss.hasPermi('community:report:handle')")
    @Log(title = "处理举报", businessType = BusinessType.UPDATE)
    @PutMapping("/handle/{reportId}/{handleResult}")
    public AjaxResult handleReport(@PathVariable Long reportId, @PathVariable String handleResult, String handleRemark)
    {
        return toAjax(communityReportService.handleReport(reportId, handleResult, handleRemark));
    }
}
