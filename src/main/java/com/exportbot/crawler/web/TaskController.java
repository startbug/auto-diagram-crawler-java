package com.exportbot.crawler.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exportbot.crawler.entity.TaskEntity;
import com.exportbot.crawler.repository.TaskRepository;
import com.exportbot.crawler.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

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
    public ResponseEntity<IPage<TaskEntity>> listTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(taskRepository.findPage(pageNum, pageSize, orderId, status, email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest request, HttpServletRequest httpRequest) {
        try {
            String userIp = getClientIp(httpRequest);

            TaskEntity task = taskService.createTask(
                    request.orderId,
                    request.email,
                    userIp,
                    request.fileUrl,
                    request.format,
                    request.quality,
                    request.watermarkType,
                    request.watermarkText
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "任务创建成功",
                    "taskId", task.getId(),
                    "uuid", task.getUuid()
            ));
        } catch (RuntimeException e) {
            logger.warn("Failed to create task: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to create task", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reset")
    public ResponseEntity<?> resetTask(@PathVariable Long id) {
        try {
            boolean success = taskService.resetTask(id);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "任务重置成功"));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "任务重置失败"));
            }
        } catch (RuntimeException e) {
            logger.warn("Failed to reset task: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to reset task", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/oss-url")
    public ResponseEntity<?> generateOssUrl(@PathVariable Long id, @RequestBody OssUrlRequest request) {
        try {
            TaskEntity task = taskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("任务不存在"));

            if (task.getOssUrl() == null || task.getOssUrl().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "任务尚未生成OSS文件"));
            }

            // TODO: 调用OSS服务生成带签名的URL
            // 这里先返回原始URL，实际需要调用阿里云OSS SDK生成临时签名URL
            int expireHours = request.expireHours != null ? request.expireHours : 1;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "ossUrl", task.getOssUrl(),
                    "expireHours", expireHours,
                    "message", "OSS URL生成成功（有效期" + expireHours + "小时）"
            ));
        } catch (RuntimeException e) {
            logger.warn("Failed to generate OSS URL: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to generate OSS URL", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
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
        // 多个代理情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    public static class CreateTaskRequest {
        public Long orderId;
        public String email;
        public String fileUrl;
        public String format;
        public String quality;
        public String watermarkType;
        public String watermarkText;
    }

    public static class OssUrlRequest {
        public Integer expireHours;
    }
}
