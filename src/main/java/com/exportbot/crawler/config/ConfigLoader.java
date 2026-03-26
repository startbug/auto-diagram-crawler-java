package com.exportbot.crawler.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final Yaml yaml;

    public ConfigLoader() {
        LoaderOptions loaderOptions = new LoaderOptions();
        this.yaml = new Yaml(loaderOptions);
    }

    public Workflow loadWorkflow(String filePath) {
        try {
            String content = Files.readString(Path.of(filePath));
            // Replace environment variables
            content = interpolateEnvVars(content);
            
            return yaml.loadAs(content, Workflow.class);
        } catch (IOException e) {
            logger.error("Failed to load workflow from: {}", filePath, e);
            throw new RuntimeException("Failed to load workflow: " + filePath, e);
        }
    }

    public Workflow loadWorkflowFromString(String content) {
        content = interpolateEnvVars(content);
        return yaml.loadAs(content, Workflow.class);
    }

    public String interpolateEnvVars(String content) {
        if (content == null) return null;
        
        Matcher matcher = ENV_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String varValue = System.getenv(varName);
            if (varValue == null) {
                varValue = System.getProperty(varName, "");
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(varValue));
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadYamlAsMap(String filePath) {
        try (InputStream is = new FileInputStream(filePath)) {
            return yaml.load(is);
        } catch (IOException e) {
            logger.error("Failed to load YAML from: {}", filePath, e);
            throw new RuntimeException("Failed to load YAML: " + filePath, e);
        }
    }
}
