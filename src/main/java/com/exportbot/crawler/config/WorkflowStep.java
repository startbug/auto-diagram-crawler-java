package com.exportbot.crawler.config;

import java.util.List;
import java.util.Map;

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

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOnError() { return onError; }
    public void setOnError(String onError) { this.onError = onError; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getSelector() { return selector; }
    public void setSelector(String selector) { this.selector = selector; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Boolean getWaitForDownload() { return waitForDownload; }
    public void setWaitForDownload(Boolean waitForDownload) { this.waitForDownload = waitForDownload; }
    public Integer getTimeout() { return timeout; }
    public void setTimeout(Integer timeout) { this.timeout = timeout; }
    public Integer getMs() { return ms; }
    public void setMs(Integer ms) { this.ms = ms; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getScript() { return script; }
    public void setScript(String script) { this.script = script; }
    public String getSaveAs() { return saveAs; }
    public void setSaveAs(String saveAs) { this.saveAs = saveAs; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getOver() { return over; }
    public void setOver(String over) { this.over = over; }
    public String getAs() { return as; }
    public void setAs(String as) { this.as = as; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }
    public String getWatermark() { return watermark; }
    public void setWatermark(String watermark) { this.watermark = watermark; }
    public String getCustomWatermarkText() { return customWatermarkText; }
    public void setCustomWatermarkText(String customWatermarkText) { this.customWatermarkText = customWatermarkText; }
    public Integer getMin() { return min; }
    public void setMin(Integer min) { this.min = min; }
    public Integer getMax() { return max; }
    public void setMax(Integer max) { this.max = max; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public String getUrlPattern() { return urlPattern; }
    public void setUrlPattern(String urlPattern) { this.urlPattern = urlPattern; }
    public String getJsonPath() { return jsonPath; }
    public void setJsonPath(String jsonPath) { this.jsonPath = jsonPath; }
    public Boolean getReturnValue() { return returnValue; }
    public void setReturnValue(Boolean returnValue) { this.returnValue = returnValue; }
    public List<WorkflowStep> getSteps() { return steps; }
    public void setSteps(List<WorkflowStep> steps) { this.steps = steps; }
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }
    public void setAdditionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties = additionalProperties; }
}
