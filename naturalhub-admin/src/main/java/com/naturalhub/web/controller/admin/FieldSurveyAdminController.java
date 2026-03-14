package com.naturalhub.web.controller.admin;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.naturalhub.common.annotation.Log;
import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.enums.BusinessType;
import com.naturalhub.common.utils.poi.ExcelUtil;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.system.domain.FieldSurvey;
import com.naturalhub.system.service.IFieldSurveyService;

/**
 * 野外调查记录管理端Controller
 *
 * @author NaturalHub
 * @date 2026-03-09
 */
@RestController
@RequestMapping("/admin/survey")
public class FieldSurveyAdminController extends BaseController
{
    @Autowired
    private IFieldSurveyService fieldSurveyService;

    @PreAuthorize("@ss.hasPermi('admin:survey:list')")
    @GetMapping("/list")
    public TableDataInfo list(FieldSurvey fieldSurvey)
    {
        startPage();
        List<FieldSurvey> list = fieldSurveyService.selectFieldSurveyList(fieldSurvey);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('admin:survey:export')")
    @Log(title = "野外调查记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FieldSurvey fieldSurvey)
    {
        List<FieldSurvey> list = fieldSurveyService.selectFieldSurveyList(fieldSurvey);
        ExcelUtil<FieldSurvey> util = new ExcelUtil<FieldSurvey>(FieldSurvey.class);
        util.exportExcel(response, list, "野外调查记录数据");
    }

    @PreAuthorize("@ss.hasPermi('admin:survey:query')")
    @GetMapping(value = "/{surveyId}")
    public AjaxResult getInfo(@PathVariable("surveyId") Long surveyId)
    {
        return success(fieldSurveyService.selectFieldSurveyBySurveyId(surveyId));
    }

    @PreAuthorize("@ss.hasPermi('admin:survey:add')")
    @Log(title = "野外调查记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FieldSurvey fieldSurvey)
    {
        return toAjax(fieldSurveyService.insertFieldSurvey(fieldSurvey));
    }

    @PreAuthorize("@ss.hasPermi('admin:survey:edit')")
    @Log(title = "野外调查记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FieldSurvey fieldSurvey)
    {
        return toAjax(fieldSurveyService.updateFieldSurvey(fieldSurvey));
    }

    @PreAuthorize("@ss.hasPermi('admin:survey:remove')")
    @Log(title = "野外调查记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{surveyIds}")
    public AjaxResult remove(@PathVariable Long[] surveyIds)
    {
        return toAjax(fieldSurveyService.deleteFieldSurveyBySurveyIds(surveyIds));
    }

    /**
     * 审核野外调查记录
     * 【改造点】getStats 中的字符串状态改为数字：pending->1, approved->2, rejected->3
     */
    @PreAuthorize("@ss.hasPermi('admin:survey:audit')")
    @Log(title = "审核野外调查记录", businessType = BusinessType.UPDATE)
    @PostMapping("/{surveyId}/audit")
    public AjaxResult audit(@PathVariable Long surveyId, @RequestBody AuditRequest request)
    {
        return toAjax(fieldSurveyService.audit(surveyId, request.getAuditStatus(), request.getAuditRemark()));
    }

    @PreAuthorize("@ss.hasPermi('admin:survey:audit')")
    @Log(title = "批量审核野外调查记录", businessType = BusinessType.UPDATE)
    @PostMapping("/audit/batch")
    public AjaxResult batchAudit(@RequestBody BatchAuditRequest request)
    {
        int count = 0;
        for (Long id : request.getIds()) {
            count += fieldSurveyService.audit(id, request.getAuditStatus(), request.getAuditRemark());
        }
        return toAjax(count);
    }

    /**
     * 获取统计数据
     * 【改造点】auditStatus 查询条件由字符串改为数字
     *   pending -> 1, approved -> 2, rejected -> 3
     */
    @PreAuthorize("@ss.hasPermi('admin:survey:list')")
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        // 【改造点】1=待审核
        FieldSurvey pending = new FieldSurvey();
        pending.setAuditStatus(1);
        int pendingCount = fieldSurveyService.selectFieldSurveyList(pending).size();

        // 【改造点】2=已通过
        FieldSurvey approved = new FieldSurvey();
        approved.setAuditStatus(2);
        int approvedCount = fieldSurveyService.selectFieldSurveyList(approved).size();

        // 【改造点】3=已驳回
        FieldSurvey rejected = new FieldSurvey();
        rejected.setAuditStatus(3);
        int rejectedCount = fieldSurveyService.selectFieldSurveyList(rejected).size();

        int totalCount = fieldSurveyService.selectFieldSurveyList(new FieldSurvey()).size();

        AjaxResult result = AjaxResult.success();
        result.put("pendingCount", pendingCount);
        result.put("approvedCount", approvedCount);
        result.put("rejectedCount", rejectedCount);
        result.put("totalCount", totalCount);
        return result;
    }

    public static class AuditRequest {
        private Integer auditStatus;
        private String auditRemark;
        public Integer getAuditStatus() { return auditStatus; }
        public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }
        public String getAuditRemark() { return auditRemark; }
        public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    }

    public static class BatchAuditRequest {
        private Long[] ids;
        private Integer auditStatus;
        private String auditRemark;
        public Long[] getIds() { return ids; }
        public void setIds(Long[] ids) { this.ids = ids; }
        public Integer getAuditStatus() { return auditStatus; }
        public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }
        public String getAuditRemark() { return auditRemark; }
        public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    }
}
