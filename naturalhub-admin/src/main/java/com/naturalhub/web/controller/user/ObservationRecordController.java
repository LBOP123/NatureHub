package com.naturalhub.web.controller.user;

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
 * 观察记录Controller
 *
 * @author naturalhub
 * @date 2025-03-03
 */
@RestController
@RequestMapping("/user/record")
public class ObservationRecordController extends BaseController {
    @Autowired
    private IObservationRecordService observationRecordService;

    /**
     * 查询观察记录列表
     */
    @GetMapping("/list")
    public TableDataInfo list(ObservationRecord observationRecord) {
        // 只查询当前用户的记录
        observationRecord.setUserId(getUserId());
        startPage();
        List<ObservationRecord> list = observationRecordService.selectObservationRecordList(observationRecord);
        return getDataTable(list);
    }

    /**
     * 导出观察记录列表
     */
    @Log(title = "观察记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ObservationRecord observationRecord) {
        observationRecord.setUserId(getUserId());
        List<ObservationRecord> list = observationRecordService.selectObservationRecordList(observationRecord);
        ExcelUtil<ObservationRecord> util = new ExcelUtil<ObservationRecord>(ObservationRecord.class);
        util.exportExcel(response, list, "观察记录数据");
    }

    /**
     * 获取观察记录详细信息
     */
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId) {
        ObservationRecord record = observationRecordService.selectObservationRecordByRecordId(recordId);
        // 验证是否是当前用户的记录
        if (record != null && !record.getUserId().equals(getUserId())) {
            return AjaxResult.error("无权访问该记录");
        }
        return AjaxResult.success(record);
    }

    /**
     * 新增观察记录
     */
    @Log(title = "观察记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ObservationRecord observationRecord) {
        observationRecord.setUserId(getUserId());
        observationRecord.setCreateBy(getUsername());
        int result = observationRecordService.insertObservationRecord(observationRecord);
        if (result > 0) {
            return AjaxResult.success(observationRecord);
        }
        return AjaxResult.error("新增失败");
    }

    /**
     * 修改观察记录
     */
    @Log(title = "观察记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ObservationRecord observationRecord) {
        // 验证是否是当前用户的记录
        ObservationRecord existRecord = observationRecordService.selectObservationRecordByRecordId(observationRecord.getRecordId());
        if (existRecord == null || !existRecord.getUserId().equals(getUserId())) {
            return AjaxResult.error("无权修改该记录");
        }
        // 如果已提交审核，不允许修改
        if (existRecord.getAuditStatus() == 1 || existRecord.getAuditStatus() == 2) {
            return AjaxResult.error("该记录已提交审核或已通过审核，无法修改");
        }
        observationRecord.setUpdateBy(getUsername());
        return toAjax(observationRecordService.updateObservationRecord(observationRecord));
    }

    /**
     * 删除观察记录
     */
    @Log(title = "观察记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds) {
        // 验证所有记录是否属于当前用户
        for (Long recordId : recordIds) {
            ObservationRecord record = observationRecordService.selectObservationRecordByRecordId(recordId);
            if (record == null || !record.getUserId().equals(getUserId())) {
                return AjaxResult.error("无权删除该记录");
            }
        }
        return toAjax(observationRecordService.deleteObservationRecordByRecordIds(recordIds));
    }

    /**
     * 提交审核
     */
    @Log(title = "观察记录", businessType = BusinessType.UPDATE)
    @PutMapping("/submit/{recordId}")
    public AjaxResult submit(@PathVariable Long recordId) {
        // 验证是否是当前用户的记录
        ObservationRecord record = observationRecordService.selectObservationRecordByRecordId(recordId);
        if (record == null || !record.getUserId().equals(getUserId())) {
            return AjaxResult.error("无权操作该记录");
        }
        // 验证状态
        if (record.getAuditStatus() != 0 && record.getAuditStatus() != 3) {
            return AjaxResult.error("只有草稿或已驳回的记录才能提交审核");
        }
        return toAjax(observationRecordService.submitForReview(recordId));
    }

    /**
     * 分享到社群
     */
    @Log(title = "观察记录", businessType = BusinessType.UPDATE)
    @PostMapping("/{recordId}/share")
    public AjaxResult shareToCommunity(@PathVariable Long recordId) {
        try {
            ObservationRecord record = observationRecordService.selectObservationRecordByRecordId(recordId);
            if (record == null || !record.getUserId().equals(getUserId())) {
                return AjaxResult.error("只能分享自己的记录");
            }

            Long topicId = observationRecordService.shareToCommunity(recordId);
            return AjaxResult.success("分享成功", topicId);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 获取审核状态
     */
    @GetMapping("/review/{recordId}")
    public AjaxResult getReviewStatus(@PathVariable Long recordId) {
        ObservationRecord record = observationRecordService.selectObservationRecordByRecordId(recordId);
        if (record == null || !record.getUserId().equals(getUserId())) {
            return AjaxResult.error("无权访问该记录");
        }
        return AjaxResult.success(record);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/stats")
    public AjaxResult getStats() {
        ObservationRecord query = new ObservationRecord();
        query.setUserId(getUserId());
        List<ObservationRecord> allRecords = observationRecordService.selectObservationRecordList(query);

        long totalRecords = allRecords.size();
        // 0 = 草稿 draft
        long draftRecords = allRecords.stream().filter(r -> r.getAuditStatus() == 0).count();
        // 1 = 待审核 pending
        long pendingRecords = allRecords.stream().filter(r -> r.getAuditStatus() == 1).count();
        // 2 = 已通过 approved
        long approvedRecords = allRecords.stream().filter(r -> r.getAuditStatus() == 2).count();
        // 3 = 已驳回 rejected
        long rejectedRecords = allRecords.stream().filter(r -> r.getAuditStatus() == 3).count();
        AjaxResult result = AjaxResult.success();
        result.put("totalRecords", totalRecords);
        result.put("draftRecords", draftRecords);
        result.put("pendingRecords", pendingRecords);
        result.put("approvedRecords", approvedRecords);
        result.put("rejectedRecords", rejectedRecords);

        return result;
    }
}
