package com.exportbot.crawler.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exportbot.crawler.dto.CreateTaskRequestDTO;
import com.exportbot.crawler.dto.CreateTaskResponseDTO;
import com.exportbot.crawler.dto.OssUrlRequestDTO;
import com.exportbot.crawler.dto.OssUrlResponseDTO;
import com.exportbot.crawler.entity.TaskEntity;
import com.exportbot.crawler.entity.common.R;
import com.exportbot.crawler.repository.TaskRepository;
import com.exportbot.crawler.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public TaskController(TaskRepository taskRepository, TaskService taskService) {
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<R<IPage<TaskEntity>>> listTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(R.success(taskRepository.findPage(pageNum, pageSize, orderId, status, email)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<R<TaskEntity>> getTask(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(entity -> ResponseEntity.ok(R.success(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<R<CreateTaskResponseDTO>> createTask(@RequestBody CreateTaskRequestDTO request, HttpServletRequest httpRequest) {
        try {
            String userIp = getClientIp(httpRequest);

            TaskEntity task = taskService.createTask(
                    request.getOrderId(),
                    request.getEmail(),
                    userIp,
                    request.getFileUrl(),
                    request.getFormat(),
                    request.getQuality(),
                    request.getWatermarkType(),
                    request.getWatermarkText()
            );

            CreateTaskResponseDTO response = new CreateTaskResponseDTO();
            response.setTaskId(task.getId());
            response.setUuid(task.getUuid());
            
            return ResponseEntity.ok(R.success(response));
        } catch (RuntimeException e) {
            logger.warn("Failed to create task: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(R.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to create task", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/reset")
    public ResponseEntity<R<Void>> resetTask(@PathVariable Long id) {
        try {
            boolean success = taskService.resetTask(id);
            if (success) {
                return ResponseEntity.ok(R.success(null));
            } else {
                return ResponseEntity.badRequest()
                        .body(R.error("任务重置失败"));
            }
        } catch (RuntimeException e) {
            logger.warn("Failed to reset task: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(R.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to reset task", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/oss-url")
    public ResponseEntity<R<OssUrlResponseDTO>> generateOssUrl(@PathVariable Long id, @RequestBody OssUrlRequestDTO request) {
        try {
            TaskEntity task = taskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("任务不存在"));

            if (task.getOssUrl() == null || task.getOssUrl().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(R.error("任务尚未生成 OSS 文件"));
            }

            // TODO: 调用 OSS 服务生成带签名的 URL
            // 这里先返回原始 URL，实际需要调用阿里云 OSS SDK 生成临时签名 URL
            int expireHours = request.getExpireHours() != null ? request.getExpireHours() : 1;

            OssUrlResponseDTO response = new OssUrlResponseDTO();
            response.setOssUrl(task.getOssUrl());
            response.setExpireHours(expireHours);
            
            return ResponseEntity.ok(R.success(response));
        } catch (RuntimeException e) {
            logger.warn("Failed to generate OSS URL: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(R.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to generate OSS URL", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理情况，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
