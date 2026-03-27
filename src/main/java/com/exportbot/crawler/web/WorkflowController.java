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
                map.put("id", w.getId());
                map.put("code", w.getCode());
                map.put("name", w.getName());
                map.put("description", w.getDescription());
                map.put("createTime", w.getCreateTime());
                map.put("modifyTime", w.getModifyTime());
                map.put("creator", w.getCreator());
                map.put("modifier", w.getModifier());
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
            map.put("id", workflow.get().getId());
            map.put("code", workflow.get().getCode());
            map.put("name", workflow.get().getName());
            map.put("description", workflow.get().getDescription());
            map.put("content", workflow.get().getContent());
            map.put("createTime", workflow.get().getCreateTime());
            map.put("modifyTime", workflow.get().getModifyTime());
            map.put("creator", workflow.get().getCreator());
            map.put("modifier", workflow.get().getModifier());
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
