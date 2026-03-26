package com.naturalhub.system.mapper;

import com.naturalhub.system.domain.SpeciesVideoTask;
import java.util.List;

/**
 * 物种科普视频生成任务 Mapper
 *
 * @author NaturalHub
 */
public interface SpeciesVideoTaskMapper {

    /** 插入任务 */
    int insertSpeciesVideoTask(SpeciesVideoTask task);

    /** 按主键查询 */
    SpeciesVideoTask selectSpeciesVideoTaskById(Long id);

    /** 列表查询（支持按 userId / taskStatus 过滤） */
    List<SpeciesVideoTask> selectSpeciesVideoTaskList(SpeciesVideoTask task);

    /** 更新任务（状态、videoUrl、errorMessage） */
    int updateSpeciesVideoTask(SpeciesVideoTask task);

    /** 按主键批量删除 */
    int deleteSpeciesVideoTaskByIds(Long[] ids);
}
