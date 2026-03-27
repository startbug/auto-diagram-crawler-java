package com.exportbot.crawler.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class Workflow {
    private String name;
    private String description;
    private int version = 1;
    private Map<String, Object> variables = new HashMap<>();
    private List<WorkflowStep> steps = new ArrayList<>();
}
