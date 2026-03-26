package com.naturalhub.system.service.impl;

import com.naturalhub.system.domain.SpeciesVideoTask;
import com.naturalhub.system.mapper.SpeciesVideoTaskMapper;
import com.naturalhub.system.service.ISpeciesVideoTaskService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 物种科普视频生成任务 ServiceImpl
 *
 * @author NaturalHub
 */
@Service
public class SpeciesVideoTaskServiceImpl implements ISpeciesVideoTaskService {

    private static final String STATUS_PENDING = "1";

    @Autowired
    private SpeciesVideoTaskMapper videoTaskMapper;

    @Autowired
    private VideoTaskAsyncExecutor asyncExecutor;

    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public SpeciesVideoTask createTask(SpeciesVideoTask task) {
        // 1. 入库，状态为等待中
        task.setTaskStatus(STATUS_PENDING);
        videoTaskMapper.insertSpeciesVideoTask(task);
        // 2. 交给独立 Bean 异步执行（@Async 代理生效），立即返回
        asyncExecutor.execute(task, httpClient);
        return task;
    }

    @Override
    public SpeciesVideoTask selectTaskById(Long id) {
        return videoTaskMapper.selectSpeciesVideoTaskById(id);
    }

    @Override
    public List<SpeciesVideoTask> selectTaskList(SpeciesVideoTask task) {
        return videoTaskMapper.selectSpeciesVideoTaskList(task);
    }

    @Override
    public int deleteTaskByIds(Long[] ids) {
        return videoTaskMapper.deleteSpeciesVideoTaskByIds(ids);
    }
}
