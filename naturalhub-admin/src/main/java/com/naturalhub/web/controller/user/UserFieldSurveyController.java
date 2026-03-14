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
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.common.utils.poi.ExcelUtil;
import com.naturalhub.system.domain.FieldSurvey;
import com.naturalhub.system.service.IFieldSurveyService;
import com.naturalhub.common.core.page.TableDataInfo;

/**
 * 野外调查记录用户端Controller
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
@RestController
@RequestMapping("/user/survey")
public class UserFieldSurveyController extends BaseController
{
    @Autowired
    private IFieldSurveyService fieldSurveyService;

    /**
     * 查询野外调查记录列表（我的调查）
     */
    @GetMapping("/list")
    public TableDataInfo list(FieldSurvey fieldSurvey)
    {
        startPage();
        // 只查询当前用户的记录
        fieldSurvey.setUserId(SecurityUtils.getUserId());
        List<FieldSurvey> list = fieldSurveyService.selectFieldSurveyList(fieldSurvey);
        return getDataTable(list);
    }

    /**
     * 查询所有调查记录列表（广场）
     */
    @GetMapping("/square")
    public TableDataInfo square(FieldSurvey fieldSurvey)
    {
        startPage();
        // 只查询审核通过的记录
        fieldSurvey.setAuditStatus(1);
        List<FieldSurvey> list = fieldSurveyService.selectFieldSurveyList(fieldSurvey);
        return getDataTable(list);
    }

    /**
     * 获取野外调查记录详细信息
     */
    @GetMapping(value = "/{surveyId}")
    public AjaxResult getInfo(@PathVariable("surveyId") Long surveyId)
    {
        return success(fieldSurveyService.selectFieldSurveyBySurveyId(surveyId));
    }

    /**
     * 新增野外调查记录
     */
    @Log(title = "野外调查记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FieldSurvey fieldSurvey)
    {
        fieldSurvey.setUserId(SecurityUtils.getUserId());
        fieldSurvey.setCreateBy(SecurityUtils.getUsername());
        return toAjax(fieldSurveyService.insertFieldSurvey(fieldSurvey));
    }

    /**
     * 修改野外调查记录
     */
    @Log(title = "野外调查记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FieldSurvey fieldSurvey)
    {
        // 验证是否是本人的记录
        FieldSurvey original = fieldSurveyService.selectFieldSurveyBySurveyId(fieldSurvey.getSurveyId());
        if (original == null || !original.getUserId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("无权修改此记录");
        }
        
        fieldSurvey.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(fieldSurveyService.updateFieldSurvey(fieldSurvey));
    }

    /**
     * 删除野外调查记录
     */
    @Log(title = "野外调查记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{surveyIds}")
    public AjaxResult remove(@PathVariable Long[] surveyIds)
    {
        // 验证是否是本人的记录
        for (Long id : surveyIds) {
            FieldSurvey survey = fieldSurveyService.selectFieldSurveyBySurveyId(id);
            if (survey == null || !survey.getUserId().equals(SecurityUtils.getUserId())) {
                return AjaxResult.error("无权删除此记录");
            }
        }
        
        return toAjax(fieldSurveyService.deleteFieldSurveyBySurveyIds(surveyIds));
    }

    /**
     * 分享到社群
     */
    @Log(title = "分享调查记录到社群", businessType = BusinessType.UPDATE)
    @PostMapping("/{surveyId}/share")
    public AjaxResult shareToCommunity(@PathVariable Long surveyId)
    {
        try {
            // 验证是否是本人的记录
            FieldSurvey survey = fieldSurveyService.selectFieldSurveyBySurveyId(surveyId);
            if (survey == null || !survey.getUserId().equals(SecurityUtils.getUserId())) {
                return AjaxResult.error("只能分享自己的记录");
            }
            
            Long topicId = fieldSurveyService.shareToCommunity(surveyId);
            return AjaxResult.success("分享成功", topicId);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 导出野外调查记录列表
     */
    @Log(title = "野外调查记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FieldSurvey fieldSurvey)
    {
        // 只导出当前用户的记录
        fieldSurvey.setUserId(SecurityUtils.getUserId());
        List<FieldSurvey> list = fieldSurveyService.selectFieldSurveyList(fieldSurvey);
        ExcelUtil<FieldSurvey> util = new ExcelUtil<FieldSurvey>(FieldSurvey.class);
        util.exportExcel(response, list, "野外调查记录数据");
    }
}
