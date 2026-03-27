package com.exportbot.crawler.service;

import com.exportbot.crawler.entity.TaskEntity;
import com.exportbot.crawler.enums.TaskStatus;
import com.exportbot.crawler.repository.OrderRepository;
import com.exportbot.crawler.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final OrderRepository orderRepository;

    public TaskService(TaskRepository taskRepository, OrderRepository orderRepository) {
        this.taskRepository = taskRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * 创建任务
     * 1. 校验订单是否存在且有可用导出次数
     * 2. 扣减订单导出次数
     * 3. 创建任务记录
     */
    @Transactional
    public TaskEntity createTask(Long orderId, String email, String userIp, String fileUrl,
                                  String format, String quality, String watermarkType, String watermarkText) {
        // 校验订单
        if (!orderRepository.hasAvailableExport(orderId)) {
            throw new RuntimeException("订单导出权益已用完");
        }

        // 扣减订单次数
        boolean success = orderRepository.incrementUsedCount(orderId);
        if (!success) {
            throw new RuntimeException("扣减导出次数失败");
        }

        // 创建任务
        TaskEntity task = new TaskEntity();
        task.setOrderId(orderId);
        task.setUuid(UUID.randomUUID().toString().replace("-", ""));
        task.setEmail(email);
        task.setUserIp(userIp);
        task.setFileUrl(fileUrl);
        task.setFormat(format != null ? format : "png");
        task.setQuality(quality != null ? quality : "high");
        task.setWatermarkType(watermarkType != null ? watermarkType : "none");
        task.setWatermarkText(watermarkText);
        task.setStatus(TaskStatus.PENDING.getCode());
        task.setResetCount(0);

        taskRepository.save(task);

        logger.info("Task created successfully: orderId={}, uuid={}", orderId, task.getUuid());
        return task;
    }

    /**
     * 重置任务
     */
    @Transactional
    public boolean resetTask(Long id) {
        Optional<TaskEntity> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("任务不存在");
        }

        TaskEntity task = taskOpt.get();

        // 只有特定状态的任务可以重置
        if (task.getStatus() != TaskStatus.FAILED.getCode()
                && task.getStatus() != TaskStatus.SUCCESS.getCode()
                && task.getStatus() != TaskStatus.DELIVERED.getCode()) {
            throw new RuntimeException("当前状态的任务不能重置");
        }

        return taskRepository.resetTask(id);
    }
}
