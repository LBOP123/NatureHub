package com.naturalhub.web.controller.admin;

import com.naturalhub.common.annotation.Log;
import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.common.enums.BusinessType;
import com.naturalhub.system.domain.BioRecognition;
import com.naturalhub.system.service.IBioRecognitionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生物识别管理Controller
 *
 * @author naturalhub
 */
@RestController
@RequestMapping("/admin/recognition")
public class BioRecognitionAdminController extends BaseController {

    @Autowired
    private IBioRecognitionRecordService bioRecognitionRecordService;

    /**
     * 查询识别记录列表
     */
    @PreAuthorize("@ss.hasPermi('admin:recognition:list')")
    @GetMapping("/list")
    public TableDataInfo list(BioRecognition bioRecognition) {
        startPage();
        List<BioRecognition> list = bioRecognitionRecordService.selectBioRecognitionList(bioRecognition);
        return getDataTable(list);
    }

    /**
     * 获取统计数据
     */
    @PreAuthorize("@ss.hasPermi('admin:recognition:query')")
    @GetMapping("/statistics")
    public AjaxResult statistics() {
        Map<String, Object> stats = new HashMap<>();

        BioRecognition query = new BioRecognition();
        List<BioRecognition> allList = bioRecognitionRecordService.selectBioRecognitionList(query);
        stats.put("totalRecognition", allList.size());

        // 【改造点】status 由字符串 "success" 改为数字 1
        long successCount = allList.stream()
                .filter(r -> Integer.valueOf(1).equals(r.getStatus()))
                .count();
        double successRate = allList.isEmpty() ? 0 : (successCount * 100.0 / allList.size());
        stats.put("successRate", Math.round(successRate * 100) / 100.0);

        stats.put("todayRecognition", allList.stream()
                .filter(r -> r.getCreateTime() != null)
                .count());

        Map<String, Long> speciesCount = new HashMap<>();
        allList.forEach(r -> {
            if (r.getRecognitionResult() != null) {
                speciesCount.put(r.getRecognitionResult(),
                        speciesCount.getOrDefault(r.getRecognitionResult(), 0L) + 1);
            }
        });
        String topSpecies = speciesCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
        stats.put("topSpecies", topSpecies);

        stats.put("trendData", getTrendData(allList));

        return AjaxResult.success(stats);
    }

    private Map<String, Object> getTrendData(List<BioRecognition> list) {
        Map<String, Object> trend = new HashMap<>();
        trend.put("dates", new String[]{"Day1", "Day2", "Day3", "Day4", "Day5", "Day6", "Day7"});
        trend.put("counts", new int[]{10, 15, 12, 18, 20, 16, 22});
        return trend;
    }

    /**
     * 查看详情
     */
    @PreAuthorize("@ss.hasPermi('admin:recognition:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(bioRecognitionRecordService.selectBioRecognitionById(id));
    }

    /**
     * 删除识别记录
     */
    @PreAuthorize("@ss.hasPermi('admin:recognition:remove')")
    @Log(title = "生物识别记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bioRecognitionRecordService.deleteBioRecognitionByIds(ids));
    }

    /**
     * 导出识别记录
     */
    @PreAuthorize("@ss.hasPermi('admin:recognition:export')")
    @Log(title = "生物识别记录", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(BioRecognition bioRecognition) {
        List<BioRecognition> list = bioRecognitionRecordService.selectBioRecognitionList(bioRecognition);
        return AjaxResult.success("导出功能开发中");
    }
}
