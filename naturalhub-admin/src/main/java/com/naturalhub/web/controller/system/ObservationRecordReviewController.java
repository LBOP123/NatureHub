package com.naturalhub.web.controller.system;

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
import com.naturalhub.system.domain.ObservationRecord;
import com.naturalhub.system.service.IObservationRecordService;
import com.naturalhub.common.utils.poi.ExcelUtil;
import com.naturalhub.common.core.page.TableDataInfo;

/**
 * 观察记录审核Controller
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
@RestController
@RequestMapping("/system/observation/record")
public class ObservationRecordReviewController extends BaseController
{
    @Autowired
    private IObservationRecordService observationRecordService;

    /**
     * 查询观察记录列表（管理端）
     */
    @PreAuthorize("@ss.hasPermi('system:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(ObservationRecord observationRecord)
    {
        startPage();
        List<ObservationRecord> list = observationRecordService.selectObservationRecordList(observationRecord);
        return getDataTable(list);
    }

    /**
     * 导出观察记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:record:export')")
    @Log(title = "观察记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ObservationRecord observationRecord)
    {
        List<ObservationRecord> list = observationRecordService.selectObservationRecordList(observationRecord);
        ExcelUtil<ObservationRecord> util = new ExcelUtil<ObservationRecord>(ObservationRecord.class);
        util.exportExcel(response, list, "观察记录数据");
    }

    /**
     * 获取观察记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:record:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return AjaxResult.success(observationRecordService.selectObservationRecordByRecordId(recordId));
    }

    /**
     * 审核通过
     */
    @PreAuthorize("@ss.hasPermi('system:record:approve')")
    @Log(title = "观察记录审核", businessType = BusinessType.UPDATE)
    @PutMapping("/approve")
    public AjaxResult approve(@RequestBody ObservationRecord observationRecord)
    {
        Long recordId = observationRecord.getRecordId();
        String reviewComment = observationRecord.getReviewComment();
        
        ObservationRecord record = observationRecordService.selectObservationRecordByRecordId(recordId);
        if (record == null) {
            return AjaxResult.error("观察记录不存在");
        }
        if (record.getAuditStatus() != 1) {
            return AjaxResult.error("只能审核待审核状态的记录");
        }
        
        record.setAuditStatus(2);
        record.setReviewComment(reviewComment);
        record.setReviewerId(getUserId());
        record.setReviewTime(new java.util.Date());
        record.setUpdateBy(getUsername());
        
        int result = observationRecordService.updateObservationRecord(record);
        
        if (result > 0) {
            // TODO: 这里可以添加生成标本数据的逻辑
            return AjaxResult.success("审核通过");
        }
        return AjaxResult.error("审核失败");
    }

    /**
     * 审核驳回
     */
    @PreAuthorize("@ss.hasPermi('system:record:reject')")
    @Log(title = "观察记录审核", businessType = BusinessType.UPDATE)
    @PutMapping("/reject")
    public AjaxResult reject(@RequestBody ObservationRecord observationRecord)
    {
        Long recordId = observationRecord.getRecordId();
        String rejectReason = observationRecord.getRejectReason();
        
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            return AjaxResult.error("请填写驳回原因");
        }
        
        ObservationRecord record = observationRecordService.selectObservationRecordByRecordId(recordId);
        if (record == null) {
            return AjaxResult.error("观察记录不存在");
        }
        if (record.getAuditStatus() != 1) {
            return AjaxResult.error("只能审核待审核状态的记录");
        }
        record.setAuditStatus(3);
        record.setRejectReason(rejectReason);
        record.setReviewerId(getUserId());
        record.setReviewTime(new java.util.Date());
        record.setUpdateBy(getUsername());
        
        return toAjax(observationRecordService.updateObservationRecord(record));
    }

    /**
     * 批量审核通过
     */
    @PreAuthorize("@ss.hasPermi('system:record:approve')")
    @Log(title = "观察记录审核", businessType = BusinessType.UPDATE)
    @PutMapping("/batchApprove")
    public AjaxResult batchApprove(@RequestBody Long[] recordIds)
    {
        int successCount = 0;
        for (Long recordId : recordIds) {
            ObservationRecord record = observationRecordService.selectObservationRecordByRecordId(recordId);
            if (record != null && record.getAuditStatus() == 1) {
                record.setAuditStatus(2);
                record.setReviewerId(getUserId());
                record.setReviewTime(new java.util.Date());
                record.setUpdateBy(getUsername());
                if (observationRecordService.updateObservationRecord(record) > 0) {
                    successCount++;
                }
            }
        }
        return AjaxResult.success("成功审核通过 " + successCount + " 条记录");
    }

    /**
     * 删除观察记录
     */
    @PreAuthorize("@ss.hasPermi('system:record:remove')")
    @Log(title = "观察记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(observationRecordService.deleteObservationRecordByRecordIds(recordIds));
    }

    /**
     * 获取统计信息
     */
    @PreAuthorize("@ss.hasPermi('system:record:list')")
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        ObservationRecord query = new ObservationRecord();
        List<ObservationRecord> allRecords = observationRecordService.selectObservationRecordList(query);

        long totalRecords = allRecords.size();
        // 1 = 待审核 pending
        long pendingRecords = allRecords.stream().filter(r -> r.getAuditStatus() == 1).count();
        // 2 = 已通过 approved
        long approvedRecords = allRecords.stream().filter(r -> r.getAuditStatus() == 2).count();
        // 3 = 已驳回 rejected
        long rejectedRecords = allRecords.stream().filter(r -> r.getAuditStatus() == 3).count();
        // 0 = 草稿 draft
        long draftRecords = allRecords.stream().filter(r -> r.getAuditStatus() == 0).count();
        AjaxResult result = AjaxResult.success();
        result.put("totalRecords", totalRecords);
        result.put("pendingRecords", pendingRecords);
        result.put("approvedRecords", approvedRecords);
        result.put("rejectedRecords", rejectedRecords);
        result.put("draftRecords", draftRecords);
        
        return result;
    }
}
