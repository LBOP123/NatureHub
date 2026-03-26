package com.naturalhub.web.controller.admin;

import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.system.domain.SpeciesVideoTask;
import com.naturalhub.system.service.ISpeciesVideoTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物种科普视频生成任务 管理端 Controller
 *
 * @author NaturalHub
 */
@RestController
@RequestMapping("/admin/video")
public class VideoAdminController extends BaseController {

    @Autowired
    private ISpeciesVideoTaskService videoTaskService;

    /**
     * 查询所有用户的视频任务列表
     */
    @PreAuthorize("@ss.hasPermi('admin:video:list')")
    @GetMapping("/list")
    public TableDataInfo list(SpeciesVideoTask task) {
        startPage();
        List<SpeciesVideoTask> list = videoTaskService.selectTaskList(task);
        return getDataTable(list);
    }

    /**
     * 查询任务详情
     */
    @PreAuthorize("@ss.hasPermi('admin:video:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(videoTaskService.selectTaskById(id));
    }

    /**
     * 删除任务（管理端）
     */
    @PreAuthorize("@ss.hasPermi('admin:video:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(videoTaskService.deleteTaskByIds(ids));
    }
}
