package com.exportbot.crawler.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exportbot.crawler.entity.TaskEntity;
import com.exportbot.crawler.mapper.TaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {

    private static final Logger logger = LoggerFactory.getLogger(TaskRepository.class);

    private final TaskMapper taskMapper;

    public TaskRepository(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public Optional<TaskEntity> findById(Long id) {
        return Optional.ofNullable(taskMapper.selectById(id));
    }

    public Optional<TaskEntity> findByUuid(String uuid) {
        return Optional.ofNullable(taskMapper.selectByUuid(uuid));
    }

    public IPage<TaskEntity> findPage(int pageNum, int pageSize, Long orderId, Integer status, String email) {
        Page<TaskEntity> page = new Page<>(pageNum, pageSize);
        return taskMapper.selectTaskPage(page, orderId, status, email);
    }

    public List<TaskEntity> findPendingTasks(int limit) {
        return taskMapper.selectPendingTasks(limit);
    }

    public void save(TaskEntity task) {
        if (task.getId() == null) {
            taskMapper.insert(task);
            logger.info("Task created: uuid={}", task.getUuid());
        } else {
            taskMapper.updateById(task);
            logger.info("Task updated: id={}", task.getId());
        }
    }

    public boolean resetTask(Long id) {
        int rows = taskMapper.resetTask(id);
        if (rows > 0) {
            logger.info("Task reset: {}", id);
        }
        return rows > 0;
    }

    public boolean updateStatus(Long id, Integer status, String errorMessage) {
        int rows = taskMapper.updateStatus(id, status, errorMessage);
        if (rows > 0) {
            logger.info("Task status updated: id={}, status={}", id, status);
        }
        return rows > 0;
    }

    public boolean updateOssUrl(Long id, String ossUrl) {
        int rows = taskMapper.updateOssUrl(id, ossUrl);
        if (rows > 0) {
            logger.info("Task OSS URL updated: id={}", id);
        }
        return rows > 0;
    }
}
