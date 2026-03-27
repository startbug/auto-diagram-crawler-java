package com.exportbot.crawler.config;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class WorkflowStep {
    private String id;
    private String type;
    private String description;
    private String onError = "skip"; // abort, skip, retry

    // Common parameters
    private String url;
    private String selector;
    private String value;
    private Boolean waitForDownload;
    private Integer timeout;
    private Integer ms;
    private String path;
    private String pattern;
    private String message;
    private String script;
    private String saveAs;
    private String condition;
    private String over;
    private String as;
    private String format;
    private String quality;
    private String watermark;
    private String customWatermarkText;

    // waitForRandomTimeout parameters
    private Integer min;
    private Integer max;

    // scroll parameters
    private String direction;
    private Integer amount;

    // waitForResponse parameters
    private String urlPattern;
    private String jsonPath;

    // evaluate parameters
    private Boolean returnValue;

    // Nested steps for loop and conditional
    private List<WorkflowStep> steps;

    // Additional parameters for flexibility
    private Map<String, Object> additionalProperties;

}
