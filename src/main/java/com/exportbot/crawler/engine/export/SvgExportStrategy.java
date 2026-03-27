package com.exportbot.crawler.engine.export;

import com.exportbot.crawler.engine.WorkflowContext;
import com.exportbot.crawler.enums.ExportFormat;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class SvgExportStrategy implements ExportStrategy {

    private static final Logger logger = LoggerFactory.getLogger(SvgExportStrategy.class);

    @Override
    public String getName() {
        return ExportFormat.SVG.getCode();
    }

    @Override
    public boolean supportsQuality() {
        return false;
    }

    @Override
    public boolean supportsWatermark() {
        return false;
    }

    @Override
    public void execute(WorkflowContext context, String quality, String watermark, String customWatermarkText) throws Exception {
        Page page = context.getPage();

        Download download = page.waitForDownload(() -> {
            page.locator("#header-export-menu > li:nth-child(6) > div > span.text").click();
        });

        Path downloadPath = Paths.get(context.getDownloadDir(), download.suggestedFilename());
        download.saveAs(downloadPath);
        context.addDownload(downloadPath.toString());

        logger.info("SVG exported successfully: {}", downloadPath);
    }
}
