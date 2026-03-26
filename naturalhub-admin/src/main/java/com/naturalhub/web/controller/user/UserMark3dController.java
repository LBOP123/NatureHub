package com.naturalhub.web.controller.user;

import com.naturalhub.common.annotation.Log;
import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.common.enums.BusinessType;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.system.domain.Mark3dTask;
import com.naturalhub.system.service.IMark3dTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/mark3d")
public class UserMark3dController extends BaseController {

    @Autowired
    private IMark3dTaskService mark3dTaskService;

    @GetMapping("/list")
    public TableDataInfo list(Mark3dTask task) {
        startPage();
        task.setUserId(SecurityUtils.getUserId());
        List<Mark3dTask> list = mark3dTaskService.selectTaskList(task);
        return getDataTable(list);
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        Mark3dTask task = mark3dTaskService.selectTaskById(id);
        if (task == null || !task.getUserId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("任务不存在或无权查看");
        }
        return AjaxResult.success(task);
    }

    @Log(title = "3D标注任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult create(@RequestBody Mark3dTask task) {
        task.setUserId(SecurityUtils.getUserId());
        task.setUsername(SecurityUtils.getUsername());
        try {
            Mark3dTask result = mark3dTaskService.createTask(task);
            return AjaxResult.success("任务创建成功，正在生成3D模型，请稍后刷新查看结果", result);
        } catch (Exception e) {
            logger.error("创建3D标注任务失败", e);
            return AjaxResult.error("创建任务失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/sync")
    public AjaxResult syncStatus(@PathVariable Long id) {
        Mark3dTask task = mark3dTaskService.selectTaskById(id);
        if (task == null || !task.getUserId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("任务不存在或无权操作");
        }
        try {
            Mark3dTask updated = mark3dTaskService.syncTaskStatus(id);
            return AjaxResult.success(updated);
        } catch (Exception e) {
            logger.error("同步任务状态失败, id={}", id, e);
            return AjaxResult.error("同步失败：" + e.getMessage());
        }
    }

    @Log(title = "3D标注任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        for (Long id : ids) {
            Mark3dTask task = mark3dTaskService.selectTaskById(id);
            if (task == null || !task.getUserId().equals(SecurityUtils.getUserId())) {
                return AjaxResult.error("包含无权删除的任务");
            }
        }
        return toAjax(mark3dTaskService.deleteTaskByIds(ids));
    }

    /** 切换公开展示状态 */
    @Log(title = "3D展馆-公开状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/public")
    public AjaxResult togglePublic(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Mark3dTask task = mark3dTaskService.selectTaskById(id);
        if (task == null || !task.getUserId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("任务不存在或无权操作");
        }
        Integer isPublic = body.get("isPublic");
        if (isPublic == null || (isPublic != 0 && isPublic != 1)) {
            return AjaxResult.error("参数错误");
        }
        mark3dTaskService.updateTaskPublic(id, isPublic);
        return AjaxResult.success(isPublic == 1 ? "已公开展示" : "已取消公开");
    }

    /** 重命名任务 */
    @Log(title = "3D标注任务-重命名", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/rename")
    public AjaxResult rename(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Mark3dTask task = mark3dTaskService.selectTaskById(id);
        if (task == null || !task.getUserId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("任务不存在或无权操作");
        }
        String taskName = body.get("taskName");
        if (taskName == null || taskName.trim().isEmpty()) {
            return AjaxResult.error("名称不能为空");
        }
        task.setTaskName(taskName.trim());
        mark3dTaskService.updateTask(task);
        return AjaxResult.success("重命名成功");
    }

    /** 查询指定用户公开的3D模型列表 */
    @GetMapping("/gallery/{userId}")
    public AjaxResult getPublicGallery(@PathVariable Long userId) {
        List<Mark3dTask> list = mark3dTaskService.selectPublicTasksByUserId(userId);
        return AjaxResult.success(list);
    }
}
