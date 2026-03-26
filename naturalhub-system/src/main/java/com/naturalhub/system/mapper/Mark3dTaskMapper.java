package com.naturalhub.system.mapper;

import com.naturalhub.system.domain.Mark3dTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 3D标注任务 Mapper
 *
 * @author NaturalHub
 */
@Mapper
public interface Mark3dTaskMapper {

    /** 插入任务 */
    int insertMark3dTask(Mark3dTask task);

    /** 根据ID查询 */
    Mark3dTask selectMark3dTaskById(Long id);

    /** 根据 Meshy 任务ID查询 */
    Mark3dTask selectMark3dTaskByMeshyTaskId(String meshyTaskId);

    /** 条件查询列表 */
    List<Mark3dTask> selectMark3dTaskList(Mark3dTask task);

    /** 更新任务 */
    int updateMark3dTask(Mark3dTask task);

    /** 删除单条 */
    int deleteMark3dTaskById(Long id);

    /** 批量删除 */
    int deleteMark3dTaskByIds(Long[] ids);

    /** 统计总数 */
    int countTotal();

    /** 统计今日新增 */
    int countToday();

    /** 统计各状态数量 */
    List<java.util.Map<String, Object>> countByStatus();

    /** 查询指定用户公开展示的已完成模型列表 */
    List<Mark3dTask> selectPublicTasksByUserId(@Param("userId") Long userId);

    /** 更新单条任务的公开状态 */
    int updateTaskPublic(@Param("id") Long id, @Param("isPublic") Integer isPublic);
}
