package com.exportbot.crawler.engine.export;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExportStrategyFactory {

    private final Map<String, ExportStrategy> strategies = new HashMap<>();

    public ExportStrategyFactory(List<ExportStrategy> strategyList) {
        for (ExportStrategy strategy : strategyList) {
            strategies.put(strategy.getName(), strategy);
        }
    }

    public ExportStrategy getStrategy(String format) {
        ExportStrategy strategy = strategies.get(format);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported export format: " + format);
        }
        return strategy;
    }
}
