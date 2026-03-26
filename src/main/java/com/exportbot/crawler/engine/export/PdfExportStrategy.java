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
public class PdfExportStrategy implements ExportStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PdfExportStrategy.class);

    @Override
    public String getName() {
        return "pdf";
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

        // Click PDF format option (direct download, no preview dialog)
        Download download = page.waitForDownload(() -> {
            page.locator("[data-format='pdf']").click();
        });

        Path downloadPath = Paths.get(context.getDownloadDir(), download.suggestedFilename());
        download.saveAs(downloadPath);
        context.addDownload(downloadPath.toString());

        logger.info("PDF exported successfully: {}", downloadPath);
    }
}
