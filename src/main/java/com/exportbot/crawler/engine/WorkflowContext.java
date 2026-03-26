package com.exportbot.crawler.engine;

import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkflowContext {

    private final Page page;
    private final String downloadDir;
    private final Map<String, Object> variables;
    private final List<String> downloads = new ArrayList<>();
    private String lastDownload;

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{([^}]+)}}");

    public WorkflowContext(Page page, String downloadDir, Map<String, Object> variables) {
        this.page = page;
        this.downloadDir = downloadDir;
        this.variables = new HashMap<>(variables != null ? variables : new HashMap<>());
    }

    public Page getPage() {
        return page;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public List<String> getDownloads() {
        return downloads;
    }

    public void addDownload(String filePath) {
        downloads.add(filePath);
        lastDownload = filePath;
    }

    public String getLastDownload() {
        return lastDownload;
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    public String interpolate(String text) {
        if (text == null) return null;

        Matcher matcher = VAR_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String varPath = matcher.group(1).trim();
            Object value = resolveVariablePath(varPath);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Object resolveVariablePath(String path) {
        String[] parts = path.split("\\.");
        Object current = variables.get(parts[0]);

        for (int i = 1; i < parts.length && current != null; i++) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(parts[i]);
            } else {
                return null;
            }
        }

        return current;
    }

    public boolean evaluateCondition(String condition) {
        if (condition == null || condition.isEmpty()) {
            return false;
        }

        // Interpolate the condition first
        String interpolated = interpolate(condition);

        // Check for truthy values
        if (interpolated == null || interpolated.isEmpty()) {
            return false;
        }

        // Boolean true
        if (interpolated.equalsIgnoreCase("true")) {
            return true;
        }

        // Non-empty string
        return !interpolated.equalsIgnoreCase("false") && !interpolated.equals("null");
    }
}
