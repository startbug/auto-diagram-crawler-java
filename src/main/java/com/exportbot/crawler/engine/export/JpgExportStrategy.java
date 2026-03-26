package com.exportbot.crawler.engine.export;

import com.exportbot.crawler.engine.WorkflowContext;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class JpgExportStrategy implements ExportStrategy {

    private static final Logger logger = LoggerFactory.getLogger(JpgExportStrategy.class);

    @Override
    public String getName() {
        return "jpg";
    }

    @Override
    public boolean supportsQuality() {
        return true;
    }

    @Override
    public boolean supportsWatermark() {
        return true;
    }

    @Override
    public void execute(WorkflowContext context, String quality, String watermark, String customWatermarkText) throws Exception {
        Page page = context.getPage();

        // Click JPG format option
        page.locator("[data-format='jpg']").click();
        logger.debug("Selected JPG format");

        // Handle quality selection
        if ("hd".equalsIgnoreCase(quality)) {
            page.locator("[data-quality='hd']").click();
            logger.debug("Selected HD quality");
        }

        // Handle watermark
        if (watermark != null && !"none".equalsIgnoreCase(watermark)) {
            if ("system".equalsIgnoreCase(watermark)) {
                page.locator("[data-watermark='system']").click();
            } else if ("custom".equalsIgnoreCase(watermark) && customWatermarkText != null) {
                page.locator("[data-watermark='custom']").click();
                page.locator("#watermark-text").fill(customWatermarkText);
            }
            logger.debug("Applied watermark: {}", watermark);
        }

        // Click confirm and wait for download
        Download download = page.waitForDownload(() -> {
            page.locator(".export-confirm-btn").click();
        });

        Path downloadPath = Paths.get(context.getDownloadDir(), download.suggestedFilename());
        download.saveAs(downloadPath);
        context.addDownload(downloadPath.toString());

        logger.info("JPG exported successfully: {}", downloadPath);
    }
}
