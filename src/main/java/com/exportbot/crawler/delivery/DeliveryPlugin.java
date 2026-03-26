package com.exportbot.crawler.delivery;

import java.util.List;
import java.util.Map;

public interface DeliveryPlugin {
    String getName();
    void initialize(Map<String, Object> options);
    DeliveryResult deliver(List<String> files, DeliveryMetadata metadata);

    record DeliveryMetadata(
            String workflowName,
            String timestamp,
            Map<String, Object> variables
    ) {}

    record DeliveryResult(
            boolean success,
            String pluginName,
            String message,
            String url
    ) {}
}
