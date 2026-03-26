package com.naturalhub.system.service;

import com.naturalhub.system.domain.SpeciesVideoTask;
import java.util.List;

/**
 * 物种科普视频生成任务 Service
 *
 * @author NaturalHub
 */
public interface ISpeciesVideoTaskService {

    /**
     * 提交视频生成任务
     * 调用扣子工作流（流式），完成后将视频转存七牛云
     *
     * @param task 任务（包含 content、userId、username、remark）
     * @return 创建后的任务（含自增 id，状态为等待中/生成中）
     */
    SpeciesVideoTask createTask(SpeciesVideoTask task);

    /**
     * 按主键查询任务
     */
    SpeciesVideoTask selectTaskById(Long id);

    /**
     * 查询当前用户任务列表
     */
    List<SpeciesVideoTask> selectTaskList(SpeciesVideoTask task);

    /**
     * 删除任务
     */
    int deleteTaskByIds(Long[] ids);
}
