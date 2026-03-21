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
import com.naturalhub.system.domain.ObservationDiary;
import com.naturalhub.system.domain.ObservationRecord;
import com.naturalhub.system.service.IObservationDiaryService;
import com.naturalhub.system.service.IDiaryRecordRelationService;
import com.naturalhub.common.utils.poi.ExcelUtil;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.common.utils.SecurityUtils;

/**
 * 个人观察日志Controller
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
@RestController
@RequestMapping("/user/diary")
public class ObservationDiaryController extends BaseController
{
    @Autowired
    private IObservationDiaryService observationDiaryService;

    @Autowired
    private IDiaryRecordRelationService diaryRecordRelationService;

    /**
     * 查询个人观察日志列表（用户端）
     */
    @GetMapping("/list")
    public TableDataInfo list(ObservationDiary observationDiary)
    {
        startPage();
        Long currentUserId = SecurityUtils.getUserId();
        Long queryUserId = observationDiary.getUserId();
        // 默认查询自己；若查询他人，仅允许公开日志
        if (queryUserId == null || currentUserId.equals(queryUserId))
        {
            observationDiary.setUserId(currentUserId);
        }
        else
        {
            observationDiary.setUserId(queryUserId);
            observationDiary.setVisibility("1");
        }
        List<ObservationDiary> list = observationDiaryService.selectObservationDiaryList(observationDiary);
        return getDataTable(list);
    }

    /**
     * 导出个人观察日志列表
     */
    @Log(title = "个人观察日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ObservationDiary observationDiary)
    {
        // 只导出当前登录用户的日志
        Long userId = SecurityUtils.getUserId();
        observationDiary.setUserId(userId);
        List<ObservationDiary> list = observationDiaryService.selectObservationDiaryList(observationDiary);
        ExcelUtil<ObservationDiary> util = new ExcelUtil<ObservationDiary>(ObservationDiary.class);
        util.exportExcel(response, list, "个人观察日志数据");
    }

    /**
     * 获取个人观察日志详细信息（包含关联的观察记录）
     */
    @GetMapping(value = "/{diaryId}")
    public AjaxResult getInfo(@PathVariable("diaryId") Long diaryId)
    {
        ObservationDiary diary = observationDiaryService.selectObservationDiaryByDiaryId(diaryId);
        if (diary == null)
        {
            return error("日志不存在");
        }
        // 自己的日志可访问；他人的日志仅允许访问公开日志
        Long currentUserId = SecurityUtils.getUserId();
        if (!currentUserId.equals(diary.getUserId()) && !"1".equals(diary.getVisibility()))
        {
            return error("无权访问该日志");
        }
        
        // 加载关联的观察记录
        List<ObservationRecord> relatedRecords = diaryRecordRelationService.getRecordsByDiaryId(diaryId);
        diary.setRelatedRecords(relatedRecords);
        
        return success(diary);
    }

    /**
     * 新增个人观察日志（包含关联的观察记录）
     */
    @Log(title = "个人观察日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ObservationDiary observationDiary)
    {
        // 设置当前登录用户ID
        observationDiary.setUserId(SecurityUtils.getUserId());
        observationDiary.setCreateBy(SecurityUtils.getUsername());
        
        int result = observationDiaryService.insertObservationDiary(observationDiary);
        
        // 处理关联的观察记录
        if (result > 0 && observationDiary.getRecordIds() != null && !observationDiary.getRecordIds().isEmpty()) {
            diaryRecordRelationService.updateRelations(observationDiary.getDiaryId(), observationDiary.getRecordIds());
        }
        
        return result > 0 ? AjaxResult.success(observationDiary) : AjaxResult.error("新增失败");
    }

    /**
     * 修改个人观察日志（包含关联的观察记录）
     */
    @Log(title = "个人观察日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ObservationDiary observationDiary)
    {
        // 验证是否是当前用户的日志
        ObservationDiary existDiary = observationDiaryService.selectObservationDiaryByDiaryId(observationDiary.getDiaryId());
        if (existDiary == null || !existDiary.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权修改该日志");
        }
        observationDiary.setUpdateBy(SecurityUtils.getUsername());
        int result = observationDiaryService.updateObservationDiary(observationDiary);
        
        // 更新关联的观察记录
        if (result > 0 && observationDiary.getRecordIds() != null) {
            diaryRecordRelationService.updateRelations(observationDiary.getDiaryId(), observationDiary.getRecordIds());
        }
        
        return toAjax(result);
    }

    /**
     * 删除个人观察日志（同时删除关联关系）
     */
    @Log(title = "个人观察日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{diaryIds}")
    public AjaxResult remove(@PathVariable Long[] diaryIds)
    {
        // 验证所有日志是否属于当前用户
        Long userId = SecurityUtils.getUserId();
        for (Long diaryId : diaryIds) {
            ObservationDiary diary = observationDiaryService.selectObservationDiaryByDiaryId(diaryId);
            if (diary == null || !diary.getUserId().equals(userId)) {
                return error("无权删除该日志");
            }
            // 删除关联关系
            diaryRecordRelationService.deleteRelationsByDiaryId(diaryId);
        }
        return toAjax(observationDiaryService.deleteObservationDiaryByDiaryIds(diaryIds));
    }

    /**
     * 归档/取消归档日志
     */
    @Log(title = "归档日志", businessType = BusinessType.UPDATE)
    @PutMapping("/archive/{diaryId}/{isArchived}")
    public AjaxResult archive(@PathVariable Long diaryId, @PathVariable String isArchived)
    {
        ObservationDiary diary = observationDiaryService.selectObservationDiaryByDiaryId(diaryId);
        if (diary == null || !diary.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权操作该日志");
        }
        diary.setIsArchived(isArchived);
        diary.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(observationDiaryService.updateObservationDiary(diary));
    }

    /**
     * 切换可见性
     */
    @Log(title = "切换日志可见性", businessType = BusinessType.UPDATE)
    @PutMapping("/visibility/{diaryId}/{visibility}")
    public AjaxResult toggleVisibility(@PathVariable Long diaryId, @PathVariable String visibility)
    {
        ObservationDiary diary = observationDiaryService.selectObservationDiaryByDiaryId(diaryId);
        if (diary == null || !diary.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权操作该日志");
        }
        diary.setVisibility(visibility);
        diary.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(observationDiaryService.updateObservationDiary(diary));
    }

    /**
     * 获取日志关联的观察记录列表
     */
    @GetMapping("/{diaryId}/records")
    public AjaxResult getRelatedRecords(@PathVariable Long diaryId)
    {
        ObservationDiary diary = observationDiaryService.selectObservationDiaryByDiaryId(diaryId);
        if (diary == null) {
            return error("日志不存在");
        }
        
        Long currentUserId = SecurityUtils.getUserId();
        if (!currentUserId.equals(diary.getUserId()) && !"1".equals(diary.getVisibility())) {
            return error("无权访问该日志");
        }
        
        List<ObservationRecord> records = diaryRecordRelationService.getRecordsByDiaryId(diaryId);
        return success(records);
    }

    /**
     * 更新日志的关联观察记录
     */
    @Log(title = "更新日志关联记录", businessType = BusinessType.UPDATE)
    @PutMapping("/{diaryId}/records")
    public AjaxResult updateRelatedRecords(@PathVariable Long diaryId, @RequestBody List<Long> recordIds)
    {
        ObservationDiary diary = observationDiaryService.selectObservationDiaryByDiaryId(diaryId);
        if (diary == null || !diary.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权操作该日志");
        }
        
        return toAjax(diaryRecordRelationService.updateRelations(diaryId, recordIds));
    }
}
