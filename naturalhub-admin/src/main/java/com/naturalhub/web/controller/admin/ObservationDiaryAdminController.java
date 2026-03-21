package com.naturalhub.web.controller.admin;

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
import com.naturalhub.common.utils.poi.ExcelUtil;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.system.domain.ObservationDiary;
import com.naturalhub.system.domain.ObservationRecord;
import com.naturalhub.system.service.IObservationDiaryService;
import com.naturalhub.system.service.IDiaryRecordRelationService;

/**
 * 个人观察日志管理端Controller
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
@RestController
@RequestMapping("/admin/diary")
public class ObservationDiaryAdminController extends BaseController
{
    @Autowired
    private IObservationDiaryService observationDiaryService;

    @Autowired
    private IDiaryRecordRelationService diaryRecordRelationService;

    /**
     * 查询个人观察日志列表（管理端）
     */
    @PreAuthorize("@ss.hasPermi('admin:diary:list')")
    @GetMapping("/list")
    public TableDataInfo list(ObservationDiary observationDiary)
    {
        startPage();
        List<ObservationDiary> list = observationDiaryService.selectObservationDiaryList(observationDiary);
        return getDataTable(list);
    }

    /**
     * 导出个人观察日志列表
     */
    @PreAuthorize("@ss.hasPermi('admin:diary:export')")
    @Log(title = "个人观察日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ObservationDiary observationDiary)
    {
        List<ObservationDiary> list = observationDiaryService.selectObservationDiaryList(observationDiary);
        ExcelUtil<ObservationDiary> util = new ExcelUtil<ObservationDiary>(ObservationDiary.class);
        util.exportExcel(response, list, "个人观察日志数据");
    }

    /**
     * 获取个人观察日志详细信息（包含关联的观察记录）
     */
    @PreAuthorize("@ss.hasPermi('admin:diary:query')")
    @GetMapping(value = "/{diaryId}")
    public AjaxResult getInfo(@PathVariable("diaryId") Long diaryId)
    {
        ObservationDiary diary = observationDiaryService.selectObservationDiaryByDiaryId(diaryId);
        if (diary == null) {
            return error("日志不存在");
        }
        
        // 加载关联的观察记录
        List<ObservationRecord> relatedRecords = diaryRecordRelationService.getRecordsByDiaryId(diaryId);
        diary.setRelatedRecords(relatedRecords);
        
        return success(diary);
    }

    /**
     * 删除个人观察日志（同时删除关联关系）
     */
    @PreAuthorize("@ss.hasPermi('admin:diary:remove')")
    @Log(title = "个人观察日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{diaryIds}")
    public AjaxResult remove(@PathVariable Long[] diaryIds)
    {
        // 删除关联关系
        for (Long diaryId : diaryIds) {
            diaryRecordRelationService.deleteRelationsByDiaryId(diaryId);
        }
        return toAjax(observationDiaryService.deleteObservationDiaryByDiaryIds(diaryIds));
    }

    /**
     * 批量归档/取消归档
     */
    @PreAuthorize("@ss.hasPermi('admin:diary:edit')")
    @Log(title = "批量归档日志", businessType = BusinessType.UPDATE)
    @PutMapping("/archive/{diaryIds}/{isArchived}")
    public AjaxResult batchArchive(@PathVariable Long[] diaryIds, @PathVariable String isArchived)
    {
        int count = 0;
        for (Long diaryId : diaryIds) {
            ObservationDiary diary = observationDiaryService.selectObservationDiaryByDiaryId(diaryId);
            if (diary != null) {
                diary.setIsArchived(isArchived);
                diary.setUpdateBy(getUsername());
                count += observationDiaryService.updateObservationDiary(diary);
            }
        }
        return toAjax(count);
    }

    /**
     * 获取日志关联的观察记录列表
     */
    @PreAuthorize("@ss.hasPermi('admin:diary:query')")
    @GetMapping("/{diaryId}/records")
    public AjaxResult getRelatedRecords(@PathVariable Long diaryId)
    {
        ObservationDiary diary = observationDiaryService.selectObservationDiaryByDiaryId(diaryId);
        if (diary == null) {
            return error("日志不存在");
        }
        
        List<ObservationRecord> records = diaryRecordRelationService.getRecordsByDiaryId(diaryId);
        return success(records);
    }

    /**
     * 获取统计数据
     */
    @PreAuthorize("@ss.hasPermi('admin:diary:list')")
    @GetMapping("/statistics")
    public AjaxResult getStatistics()
    {
        // 总数
        int totalCount = observationDiaryService.selectObservationDiaryList(new ObservationDiary()).size();
        
        // 公开日志数量
        ObservationDiary publicDiary = new ObservationDiary();
        publicDiary.setVisibility("1");
        int publicCount = observationDiaryService.selectObservationDiaryList(publicDiary).size();
        
        // 私密日志数量
        ObservationDiary privateDiary = new ObservationDiary();
        privateDiary.setVisibility("0");
        int privateCount = observationDiaryService.selectObservationDiaryList(privateDiary).size();
        
        // 已归档数量
        ObservationDiary archivedDiary = new ObservationDiary();
        archivedDiary.setIsArchived("1");
        int archivedCount = observationDiaryService.selectObservationDiaryList(archivedDiary).size();
        
        AjaxResult result = AjaxResult.success();
        result.put("total", totalCount);
        result.put("publicCount", publicCount);
        result.put("privateCount", privateCount);
        result.put("archivedCount", archivedCount);
        return result;
    }
}
