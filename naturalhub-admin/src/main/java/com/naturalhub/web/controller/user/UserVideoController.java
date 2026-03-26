package com.naturalhub.web.controller.user;

import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.system.domain.QaHistory;
import com.naturalhub.system.domain.SpeciesVideoTask;
import com.naturalhub.system.service.IQaHistoryService;
import com.naturalhub.system.service.ISpeciesVideoTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物种科普视频生成任务 用户端 Controller
 *
 * @author NaturalHub
 */
@RestController
@RequestMapping("/user/video")
public class UserVideoController extends BaseController {

    @Autowired
    private ISpeciesVideoTaskService videoTaskService;

    @Autowired
    private IQaHistoryService qaHistoryService;

    /**
     * 提交视频生成任务
     * 1. 立即写入 qa_history（answer=[GENERATING]），拿到 historyId
     * 2. 创建 species_video_task，带上 historyId
     * 3. 异步执行，立即返回
     */
    @PostMapping
    public AjaxResult create(@RequestBody SpeciesVideoTask task) {
        try {
            Long userId   = SecurityUtils.getUserId();
            String username = SecurityUtils.getUsername();
            task.setUserId(userId);
            task.setUsername(username);

            // 立即写入一条 qa_history，answer 用占位符 [GENERATING]
            if (task.getConversationId() != null) {
                QaHistory history = new QaHistory();
                history.setConversationId(task.getConversationId());
                history.setUsername(username);
                String questionTitle = (task.getRemark() != null && !task.getRemark().isEmpty())
                        ? task.getRemark() : "科普视频";
                history.setQuestion("[视频生成] " + questionTitle);
                history.setAnswer("[GENERATING]");
                history.setQaType(2);
                qaHistoryService.saveQaHistory(history);
                // 将 historyId 传给任务，异步完成后回填
                task.setQaHistoryId(history.getId());
            }

            SpeciesVideoTask result = videoTaskService.createTask(task);
            return AjaxResult.success("视频生成任务已提交", result);
        } catch (Exception e) {
            logger.error("提交视频生成任务失败", e);
            return AjaxResult.error("提交失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        SpeciesVideoTask task = videoTaskService.selectTaskById(id);
        if (task == null || !task.getUserId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("任务不存在或无权查看");
        }
        return AjaxResult.success(task);
    }

    @GetMapping("/list")
    public TableDataInfo list(SpeciesVideoTask task) {
        startPage();
        task.setUserId(SecurityUtils.getUserId());
        List<SpeciesVideoTask> list = videoTaskService.selectTaskList(task);
        return getDataTable(list);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        for (Long id : ids) {
            SpeciesVideoTask task = videoTaskService.selectTaskById(id);
            if (task == null || !task.getUserId().equals(SecurityUtils.getUserId())) {
                return AjaxResult.error("包含无权删除的任务");
            }
        }
        return toAjax(videoTaskService.deleteTaskByIds(ids));
    }
}
