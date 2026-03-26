package com.naturalhub.system.service;

import com.naturalhub.system.domain.Mark3dTask;

import java.util.List;
import java.util.Map;

/**
 * 3D标注任务 Service 接口
 *
 * @author NaturalHub
 */
public interface IMark3dTaskService {

    /**
     * 创建3D标注任务（调用腾讯云混元3D API 并保存记录）
     */
    Mark3dTask createTask(Mark3dTask task) throws Exception;

    /**
     * 查询当前用户的任务列表
     */
    List<Mark3dTask> selectTaskListByUser(Long userId);

    /**
     * 查询任务列表（管理端，支持多条件）
     */
    List<Mark3dTask> selectTaskList(Mark3dTask task);

    /**
     * 根据ID查询任务详情
     */
    Mark3dTask selectTaskById(Long id);

    /**
     * 同步腾讯云混元3D最新状态到数据库
     */
    Mark3dTask syncTaskStatus(Long id) throws Exception;

    /**
     * 删除任务
     */
    int deleteTaskByIds(Long[] ids);

    /**
     * 获取统计数据（管理端）
     */
    Map<String, Object> getStatistics();

    /**
     * 查询指定用户公开展示的已完成3D模型列表（用于3D展馆）
     */
    List<Mark3dTask> selectPublicTasksByUserId(Long userId);

    /**
     * 更新单条任务的公开展示状态
     *
     * @param id       任务ID
     * @param isPublic 0-不公开 1-公开
     */
    int updateTaskPublic(Long id, Integer isPublic);


    /**
     * 更新任务信息（用于重命名等）
     */
    int updateTask(Mark3dTask task);
}
