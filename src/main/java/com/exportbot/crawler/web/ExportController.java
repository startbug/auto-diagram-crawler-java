package com.exportbot.crawler.web;

import com.exportbot.crawler.config.ConfigLoader;
import com.exportbot.crawler.config.Workflow;
import com.exportbot.crawler.entity.common.R;
import com.exportbot.crawler.pipeline.PipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

    private final PipelineService pipelineService;
    private final ConfigLoader configLoader;

    public ExportController(PipelineService pipelineService, ConfigLoader configLoader) {
        this.pipelineService = pipelineService;
        this.configLoader = configLoader;
    }

    @PostMapping("/trigger")
    public ResponseEntity<R<Map<String, Object>>> triggerExport(@RequestBody(required = false) TriggerRequest request) {
        logger.info("Export triggered via API");

        try {
            Map<String, Object> variables = new HashMap<>();
            if (request != null) {
                if (request.url != null) variables.put("url", request.url);
                if (request.password != null) variables.put("password", request.password);
                if (request.format != null) variables.put("format", request.format);
                if (request.quality != null) variables.put("quality", request.quality);
                if (request.watermark != null) variables.put("watermark", request.watermark);
                // 传入邮件接收人列表（支持多个，逗号分隔）
                if (request.emailRecipients != null && !request.emailRecipients.isEmpty()) {
                    variables.put("emailRecipients", request.emailRecipients);
                }
            }

            String workflowId = request != null ? request.workflowId : null;
            var result = pipelineService.runPipeline(workflowId, false, variables);

            Map<String, Object> data = new HashMap<>();
            data.put("downloads", result.workflow().downloads());
            data.put("delivery", result.delivery());
            data.put("duration", result.totalDuration());
            
            return ResponseEntity.ok(R.success(data));
        } catch (Exception e) {
            logger.error("Export failed", e);
            return ResponseEntity.internalServerError().body(R.error(e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<R<Map<String, Object>>> validateWorkflow(@RequestBody(required = false) ValidateRequest request) {
        try {
            String workflowPath = request != null && request.workflow != null
                    ? request.workflow
                    : "./config/workflows/example-export.yaml";

            Workflow workflow = configLoader.loadWorkflow(workflowPath);

            Map<String, Object> data = new HashMap<>();
            data.put("name", workflow.getName());
            data.put("steps", workflow.getSteps().size());
            data.put("variables", workflow.getVariables().keySet());

            return ResponseEntity.ok(R.success(data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(R.error(e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<R<Map<String, Object>>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ok");
        data.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(R.success(data));
    }

    public static class TriggerRequest {
        public String workflowId;  // 工作流 ID（从数据库加载）
        public String url;
        public String password;
        public String format;
        public String quality;
        public String watermark;
        public String emailRecipients;  // 邮件接收人列表，多个逗号分隔
    }

    public static class ValidateRequest {
        public String workflow;
    }
}
