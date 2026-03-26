package com.exportbot.crawler.browser;

import com.exportbot.crawler.config.AppConfig;
import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class BrowserFactory {

    private static final Logger logger = LoggerFactory.getLogger(BrowserFactory.class);

    public BrowserInstance createBrowser(AppConfig config) {
        AppConfig.BrowserConfig browserConfig = config.getBrowser();
        String downloadDir = browserConfig.getDownloadDir();

        // Ensure download directory exists
        Path downloadPath = Paths.get(downloadDir).toAbsolutePath();
        downloadPath.toFile().mkdirs();

        logger.info("Launching browser: headless={}, slowMo={}", browserConfig.isHeadless(), browserConfig.getSlowMo());

        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(browserConfig.isHeadless())
                .setSlowMo(browserConfig.getSlowMo()));

        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setAcceptDownloads(true));

        context.setDefaultTimeout(browserConfig.getTimeout());

        Page page = context.newPage();

        logger.info("Browser ready");

        return new BrowserInstance(playwright, browser, context, page, downloadDir);
    }

    public record BrowserInstance(
            Playwright playwright,
            Browser browser,
            BrowserContext context,
            Page page,
            String downloadDir
    ) {
        public void close() {
            try {
                page.close();
                context.close();
                browser.close();
                playwright.close();
                LoggerFactory.getLogger(BrowserInstance.class).info("Browser closed");
            } catch (Exception e) {
                LoggerFactory.getLogger(BrowserInstance.class).warn("Error closing browser", e);
            }
        }
    }
}
