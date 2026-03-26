package com.exportbot.crawler.engine.export;

import com.exportbot.crawler.engine.WorkflowContext;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PngExportStrategy implements ExportStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PngExportStrategy.class);

    @Override
    public String getName() {
        return "png";
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

        // Click PNG format option
        page.locator("#header-export-menu > li:nth-child(1) > div > span.po-menu-icon.po-diagraming-icons").click();
        logger.debug("Selected PNG format");

        // Handle quality selection if needed
        if ("hd".equalsIgnoreCase(quality)) {
            page.locator(
                            "body > div.water_mark_bg > div > div.water_content_detail > div.water_mark_choose_detail > div.w_m_f_set_up_hd > div:nth-child(1) > input")
                    .click();
            logger.debug("Selected HD quality");
        } else {
            page.locator(
                            "body > div.water_mark_bg > div > div.water_content_detail > div.water_mark_choose_detail > div.w_m_f_set_up_hd > div:nth-child(2) > input")
                    .click();
            logger.debug("Selected Normal quality");
        }

        // Handle watermark

        if ("system".equals(watermark)) {
            page.locator(
                            "body > div.water_mark_bg > div > div.water_content_detail > div.water_mark_choose_detail > div.w_m_f_set_up > div:nth-child(1) > span")
                    .click();
            logger.debug("选择系统水印");
        } else if ("custom".equals(watermark)) {
            page.locator(
                            "body > div.water_mark_bg > div > div.water_content_detail > div.water_mark_choose_detail > div.w_m_f_set_up > div:nth-child(3) > input")
                    .click();
            logger.debug("选择自定义水印");
        } else {
            page.locator(
                            "body > div.water_mark_bg > div > div.water_content_detail > div.water_mark_choose_detail > div.w_m_f_set_up > div:nth-child(2) > span")
                    .click();
            logger.debug("选择无水印");
        }
        logger.debug("Applied watermark: {}", watermark);

        // Click confirm and wait for download
        Download download = page.waitForDownload(() -> {
            page.locator(
                            "body > div.water_mark_bg > div > div.water_content_detail > div.water_mark_choose_detail > div.w_m_r_bottom > div.w_m_start_out")
                    .click();
        });

        Path downloadPath = Paths.get(context.getDownloadDir(), download.suggestedFilename());
        download.saveAs(downloadPath);
        context.addDownload(downloadPath.toString());

        logger.info("PNG exported successfully: {}", downloadPath);
    }
}
