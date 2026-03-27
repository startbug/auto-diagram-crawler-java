package com.exportbot.crawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "crawler")
public class AppConfig {

    private ServerConfig server = new ServerConfig();
    private BrowserConfig browser = new BrowserConfig();
    private AuthConfig auth = new AuthConfig();
    private WorkflowConfig workflow = new WorkflowConfig();
    private ScheduleConfig schedule = new ScheduleConfig();
    private List<DeliveryPluginConfig> delivery;

    @Data
    public static class ServerConfig {
        private int port = 3000;
        private String host = "127.0.0.1";
    }

    @Data
    public static class BrowserConfig {
        private boolean headless = false;
        private int slowMo = 50;
        private int timeout = 30000;
        private String downloadDir = "./data/downloads";
    }

    @Data
    public static class AuthConfig {
        private String storePath = "./data/auth.json";
    }

    @Data
    public static class WorkflowConfig {
        private String file = "./config/workflows/example-export.yaml";
    }

    @Data
    public static class ScheduleConfig {
        private boolean enabled = false;
        private String cron = "0 9 * * 1";
    }

    @Data
    public static class DeliveryPluginConfig {
        private String type;
        private boolean enabled = false;
        private java.util.Map<String, Object> options;
    }

}
