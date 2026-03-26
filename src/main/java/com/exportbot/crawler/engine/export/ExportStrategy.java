package com.exportbot.crawler.engine.export;

import com.exportbot.crawler.engine.WorkflowContext;

public interface ExportStrategy {
    String getName();
    boolean supportsQuality();
    boolean supportsWatermark();
    void execute(WorkflowContext context, String quality, String watermark, String customWatermarkText) throws Exception;
}
