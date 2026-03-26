package com.exportbot.crawler.delivery;

import com.exportbot.crawler.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.*;

@Component
public class DeliveryManager {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryManager.class);

    private final Map<String, DeliveryPlugin> plugins = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public DeliveryManager() {
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public void initialize(List<AppConfig.DeliveryPluginConfig> configs) {
        if (configs == null) return;

        for (AppConfig.DeliveryPluginConfig config : configs) {
            if (!config.isEnabled()) continue;

            DeliveryPlugin plugin = createPlugin(config.getType());
            if (plugin != null) {
                plugin.initialize(config.getOptions());
                plugins.put(plugin.getName(), plugin);
                logger.info("Initialized delivery plugin: {}", plugin.getName());
            }
        }
    }

    public List<DeliveryPlugin.DeliveryResult> deliverAll(List<String> files, DeliveryPlugin.DeliveryMetadata metadata) {
        List<DeliveryPlugin.DeliveryResult> results = new ArrayList<>();

        if (plugins.isEmpty()) {
            logger.warn("No delivery plugins configured");
            return results;
        }

        List<Future<DeliveryPlugin.DeliveryResult>> futures = new ArrayList<>();

        for (DeliveryPlugin plugin : plugins.values()) {
            Future<DeliveryPlugin.DeliveryResult> future = executorService.submit(() -> {
                try {
                    return plugin.deliver(files, metadata);
                } catch (Exception e) {
                    logger.error("Delivery failed for plugin: {}", plugin.getName(), e);
                    return new DeliveryPlugin.DeliveryResult(
                            false, plugin.getName(), e.getMessage(), null
                    );
                }
            });
            futures.add(future);
        }

        for (Future<DeliveryPlugin.DeliveryResult> future : futures) {
            try {
                results.add(future.get(60, TimeUnit.SECONDS));
            } catch (Exception e) {
                logger.error("Failed to get delivery result", e);
                results.add(new DeliveryPlugin.DeliveryResult(
                        false, "unknown", e.getMessage(), null
                ));
            }
        }

        return results;
    }

    private DeliveryPlugin createPlugin(String type) {
        return switch (type) {
            case "smtp" -> new com.exportbot.crawler.delivery.plugins.SmtpPlugin();
            case "aliyun-oss" -> new com.exportbot.crawler.delivery.plugins.AliyunOssPlugin();
            default -> {
                logger.error("Unknown delivery plugin type: {}", type);
                yield null;
            }
        };
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
