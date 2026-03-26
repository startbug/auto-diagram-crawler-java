package com.exportbot.crawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "crawler")
public class AppConfig {

    private ServerConfig server = new ServerConfig();
    private BrowserConfig browser = new BrowserConfig();
    private AuthConfig auth = new AuthConfig();
    private WorkflowConfig workflow = new WorkflowConfig();
    private ScheduleConfig schedule = new ScheduleConfig();
    private List<DeliveryPluginConfig> delivery;

    public static class ServerConfig {
        private int port = 3000;
        private String host = "127.0.0.1";

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
    }

    public static class BrowserConfig {
        private boolean headless = false;
        private int slowMo = 50;
        private int timeout = 30000;
        private String downloadDir = "./data/downloads";

        public boolean isHeadless() { return headless; }
        public void setHeadless(boolean headless) { this.headless = headless; }
        public int getSlowMo() { return slowMo; }
        public void setSlowMo(int slowMo) { this.slowMo = slowMo; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public String getDownloadDir() { return downloadDir; }
        public void setDownloadDir(String downloadDir) { this.downloadDir = downloadDir; }
    }

    public static class AuthConfig {
        private String storePath = "./data/auth.json";

        public String getStorePath() { return storePath; }
        public void setStorePath(String storePath) { this.storePath = storePath; }
    }

    public static class WorkflowConfig {
        private String file = "./config/workflows/example-export.yaml";

        public String getFile() { return file; }
        public void setFile(String file) { this.file = file; }
    }

    public static class ScheduleConfig {
        private boolean enabled = false;
        private String cron = "0 9 * * 1";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getCron() { return cron; }
        public void setCron(String cron) { this.cron = cron; }
    }

    public static class DeliveryPluginConfig {
        private String type;
        private boolean enabled = false;
        private java.util.Map<String, Object> options;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public java.util.Map<String, Object> getOptions() { return options; }
        public void setOptions(java.util.Map<String, Object> options) { this.options = options; }
    }

    // Getters and Setters
    public ServerConfig getServer() { return server; }
    public void setServer(ServerConfig server) { this.server = server; }
    public BrowserConfig getBrowser() { return browser; }
    public void setBrowser(BrowserConfig browser) { this.browser = browser; }
    public AuthConfig getAuth() { return auth; }
    public void setAuth(AuthConfig auth) { this.auth = auth; }
    public WorkflowConfig getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowConfig workflow) { this.workflow = workflow; }
    public ScheduleConfig getSchedule() { return schedule; }
    public void setSchedule(ScheduleConfig schedule) { this.schedule = schedule; }
    public List<DeliveryPluginConfig> getDelivery() { return delivery; }
    public void setDelivery(List<DeliveryPluginConfig> delivery) { this.delivery = delivery; }
}
