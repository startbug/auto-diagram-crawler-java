package com.exportbot.crawler.web;

import com.exportbot.crawler.config.ConfigLoader;
import com.exportbot.crawler.config.Workflow;
import com.exportbot.crawler.pipeline.PipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Map<String, Object>> triggerExport(@RequestBody(required = false) TriggerRequest request) {
        logger.info("Export triggered via API");

        try {
            Map<String, Object> variables = new HashMap<>();
            if (request != null) {
                if (request.url != null) variables.put("url", request.url);
                if (request.password != null) variables.put("password", request.password);
                if (request.format != null) variables.put("format", request.format);
                if (request.quality != null) variables.put("quality", request.quality);
                if (request.watermark != null) variables.put("watermark", request.watermark);
            }

            String workflowId = request != null ? request.workflowId : null;
            var result = pipelineService.runPipeline(workflowId, false, variables);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.workflow().success());
            response.put("downloads", result.workflow().downloads());
            response.put("delivery", result.delivery());
            response.put("duration", result.totalDuration());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Export failed", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateWorkflow(@RequestBody(required = false) ValidateRequest request) {
        try {
            String workflowPath = request != null && request.workflow != null
                    ? request.workflow
                    : "./config/workflows/example-export.yaml";

            Workflow workflow = configLoader.loadWorkflow(workflowPath);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("name", workflow.getName());
            response.put("steps", workflow.getSteps().size());
            response.put("variables", workflow.getVariables().keySet());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    public static class TriggerRequest {
        public String workflowId;  // 工作流ID（从数据库加载）
        public String url;
        public String password;
        public String format;
        public String quality;
        public String watermark;
    }

    public static class ValidateRequest {
        public String workflow;
    }
}
