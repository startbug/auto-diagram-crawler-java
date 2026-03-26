package com.exportbot.crawler.web;

import com.exportbot.crawler.repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);

    private final WorkflowRepository workflowRepository;

    public WorkflowController(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listWorkflows() {
        var workflows = workflowRepository.findAll().stream()
            .map(w -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", w.id());
                map.put("code", w.code());
                map.put("name", w.name());
                map.put("description", w.description());
                map.put("updatedAt", w.updatedAt());
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(workflows);
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getWorkflow(@PathVariable String code) {
        var workflow = workflowRepository.findByCode(code);
        if (workflow.isPresent()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", workflow.get().id());
            map.put("code", workflow.get().code());
            map.put("name", workflow.get().name());
            map.put("description", workflow.get().description());
            map.put("content", workflow.get().content());
            map.put("updatedAt", workflow.get().updatedAt());
            return ResponseEntity.ok(map);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> saveWorkflow(@RequestBody SaveRequest request) {
        try {
            workflowRepository.save(request.code, request.name, request.description, request.content);
            return ResponseEntity.ok(Map.of("success", true, "message", "Workflow saved"));
        } catch (Exception e) {
            logger.error("Failed to save workflow", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<?> deleteWorkflow(@PathVariable String code) {
        try {
            workflowRepository.delete(code);
            return ResponseEntity.ok(Map.of("success", true, "message", "Workflow deleted"));
        } catch (Exception e) {
            logger.error("Failed to delete workflow", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    public static class SaveRequest {
        public String code;
        public String name;
        public String description;
        public String content;
    }
}
