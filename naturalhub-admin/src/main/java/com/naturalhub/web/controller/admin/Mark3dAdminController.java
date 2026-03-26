package com.naturalhub.web.controller.admin;

import com.naturalhub.common.annotation.Log;
import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.common.enums.BusinessType;
import com.naturalhub.system.domain.Mark3dTask;
import com.naturalhub.system.service.IMark3dTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/mark3d")
public class Mark3dAdminController extends BaseController {

    @Autowired
    private IMark3dTaskService mark3dTaskService;

    @PreAuthorize("@ss.hasPermi('admin:mark3d:list')")
    @GetMapping("/list")
    public TableDataInfo list(Mark3dTask task) {
        startPage();
        List<Mark3dTask> list = mark3dTaskService.selectTaskList(task);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('admin:mark3d:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(mark3dTaskService.selectTaskById(id));
    }

    @PreAuthorize("@ss.hasPermi('admin:mark3d:edit')")
    @PostMapping("/{id}/sync")
    public AjaxResult syncStatus(@PathVariable Long id) {
        try {
            Mark3dTask updated = mark3dTaskService.syncTaskStatus(id);
            return AjaxResult.success(updated);
        } catch (Exception e) {
            logger.error("管理端同步任务状态失败, id={}", id, e);
            return AjaxResult.error("同步失败：" + e.getMessage());
        }
    }

    /**
     * 管理端修改任务公开状态
     * PUT /admin/mark3d/{id}/public  body: {"isPublic": 0 or 1}
     */
    @PreAuthorize("@ss.hasPermi('admin:mark3d:edit')")
    @Log(title = "3D标注管理-公开状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/public")
    public AjaxResult updatePublic(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer isPublic = body.get("isPublic");
        if (isPublic == null || (isPublic != 0 && isPublic != 1)) {
            return AjaxResult.error("参数错误");
        }
        mark3dTaskService.updateTaskPublic(id, isPublic);
        return AjaxResult.success(isPublic == 1 ? "已设为公开" : "已取消公开");
    }

    @PreAuthorize("@ss.hasPermi('admin:mark3d:remove')")
    @Log(title = "3D标注任务管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(mark3dTaskService.deleteTaskByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('admin:mark3d:query')")
    @GetMapping("/statistics")
    public AjaxResult statistics() {
        Map<String, Object> stats = mark3dTaskService.getStatistics();
        return AjaxResult.success(stats);
    }
}
