package com.exportbot.crawler.engine;

import com.exportbot.crawler.config.WorkflowStep;
import com.exportbot.crawler.engine.export.ExportStrategyFactory;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
public class StepExecutor {

    private static final Logger logger = LoggerFactory.getLogger(StepExecutor.class);

    private final ExportStrategyFactory exportStrategyFactory;

    public StepExecutor(ExportStrategyFactory exportStrategyFactory) {
        this.exportStrategyFactory = exportStrategyFactory;
    }

    public void execute(WorkflowStep step, WorkflowContext context) throws Exception {
        String type = step.getType();
        
        switch (type) {
            case "navigate" -> executeNavigate(step, context);
            case "click" -> executeClick(step, context);
            case "fill" -> executeFill(step, context);
            case "select" -> executeSelect(step, context);
            case "waitForSelector" -> executeWaitForSelector(step, context);
            case "waitForTimeout" -> executeWaitForTimeout(step, context);
            case "evaluate" -> executeEvaluate(step, context);
            case "screenshot" -> executeScreenshot(step, context);
            case "renameDownload" -> executeRenameDownload(step, context);
            case "log" -> executeLog(step, context);
            case "loop" -> executeLoop(step, context);
            case "conditional" -> executeConditional(step, context);
            case "exportWithStrategy" -> executeExportWithStrategy(step, context);
            default -> throw new UnsupportedOperationException("Unknown step type: " + type);
        }
    }

    private void executeNavigate(WorkflowStep step, WorkflowContext context) {
        String url = context.interpolate(step.getUrl());
        Integer timeout = step.getTimeout();

        logger.info("Navigating to: {}", url);
        Page page = context.getPage();

        if (timeout != null) {
            page.navigate(url, new Page.NavigateOptions().setTimeout(timeout));
        } else {
            page.navigate(url);
        }
    }

    private void executeClick(WorkflowStep step, WorkflowContext context) throws Exception {
        String selector = context.interpolate(step.getSelector());
        Boolean waitForDownload = step.getWaitForDownload();
        Integer timeout = step.getTimeout();

        logger.info("Clicking element: {}", selector);
        Page page = context.getPage();
        Locator element = page.locator(selector);

        if (Boolean.TRUE.equals(waitForDownload)) {
            Download download = page.waitForDownload(() -> element.click());
            Path downloadPath = Paths.get(context.getDownloadDir(), download.suggestedFilename());
            download.saveAs(downloadPath);
            context.addDownload(downloadPath.toString());
            logger.info("Downloaded file: {}", downloadPath);
        } else {
            element.click();
        }
    }

    private void executeFill(WorkflowStep step, WorkflowContext context) {
        String selector = context.interpolate(step.getSelector());
        String value = context.interpolate(step.getValue());

        logger.info("Filling element: {} with value: {}", selector, value);
        Page page = context.getPage();
        page.locator(selector).fill(value);
    }

    private void executeSelect(WorkflowStep step, WorkflowContext context) {
        String selector = context.interpolate(step.getSelector());
        String value = context.interpolate(step.getValue());

        logger.info("Selecting option: {} in element: {}", value, selector);
        Page page = context.getPage();
        page.locator(selector).selectOption(value);
    }

    private void executeWaitForSelector(WorkflowStep step, WorkflowContext context) {
        String selector = context.interpolate(step.getSelector());
        Integer timeout = step.getTimeout();

        logger.info("Waiting for selector: {}", selector);
        Page page = context.getPage();
        
        Locator.WaitForOptions options = new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE);
        if (timeout != null) {
            options.setTimeout(timeout);
        }
        
        page.locator(selector).waitFor(options);
    }

    private void executeWaitForTimeout(WorkflowStep step, WorkflowContext context) throws InterruptedException {
        Integer ms = step.getMs() != null ? step.getMs() : step.getTimeout();
        if (ms == null) ms = 1000;

        logger.info("Waiting for {} ms", ms);
        Thread.sleep(ms);
    }

    private void executeEvaluate(WorkflowStep step, WorkflowContext context) {
        String script = step.getScript();
        String saveAs = step.getSaveAs();

        logger.info("Executing JavaScript script");
        Page page = context.getPage();
        Object result = page.evaluate(script);

        if (saveAs != null) {
            context.setVariable(saveAs, result);
            logger.info("Saved result to variable: {}", saveAs);
        }
    }

    private void executeScreenshot(WorkflowStep step, WorkflowContext context) {
        String path = step.getPath();
        if (path == null) {
            path = Paths.get(context.getDownloadDir(), "screenshot_" + System.currentTimeMillis() + ".png").toString();
        }
        path = context.interpolate(path);

        logger.info("Taking screenshot: {}", path);
        Page page = context.getPage();
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(path)));
        context.addDownload(path);
    }

    private void executeRenameDownload(WorkflowStep step, WorkflowContext context) {
        String pattern = context.interpolate(step.getPattern());
        String lastDownload = context.getLastDownload();

        if (lastDownload == null) {
            throw new IllegalStateException("No download to rename");
        }

        Path sourcePath = Paths.get(lastDownload);
        Path targetPath = Paths.get(context.getDownloadDir(), pattern);

        try {
            java.nio.file.Files.move(sourcePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            context.addDownload(targetPath.toString());
            logger.info("Renamed download: {} -> {}", sourcePath, targetPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to rename download", e);
        }
    }

    private void executeLog(WorkflowStep step, WorkflowContext context) {
        String message = context.interpolate(step.getMessage());
        logger.info("[LOG] {}", message);
    }

    @SuppressWarnings("unchecked")
    private void executeLoop(WorkflowStep step, WorkflowContext context) throws Exception {
        String overVar = context.interpolate(step.getOver());
        String asVar = step.getAs();
        List<WorkflowStep> subSteps = step.getSteps();

        Object overValue = context.getVariable(overVar);
        if (!(overValue instanceof List)) {
            throw new IllegalArgumentException("Loop variable '" + overVar + "' is not a list");
        }

        List<Object> list = (List<Object>) overValue;
        logger.info("Starting loop over {} items", list.size());

        for (int i = 0; i < list.size(); i++) {
            context.setVariable(asVar, list.get(i));
            context.setVariable(asVar + "_index", i);

            logger.debug("Loop iteration {}/{}: {} = {}", i + 1, list.size(), asVar, list.get(i));

            for (WorkflowStep subStep : subSteps) {
                execute(subStep, context);
            }
        }
    }

    private void executeConditional(WorkflowStep step, WorkflowContext context) throws Exception {
        String condition = step.getCondition();
        List<WorkflowStep> subSteps = step.getSteps();

        boolean shouldExecute = context.evaluateCondition(condition);
        logger.info("Conditional step: condition='{}', result={}", condition, shouldExecute);

        if (shouldExecute) {
            for (WorkflowStep subStep : subSteps) {
                execute(subStep, context);
            }
        }
    }

    private void executeExportWithStrategy(WorkflowStep step, WorkflowContext context) throws Exception {
        String format = context.interpolate(step.getFormat());
        String quality = context.interpolate(step.getQuality());
        String watermark = context.interpolate(step.getWatermark());
        String customWatermarkText = context.interpolate(step.getCustomWatermarkText());

        logger.info("Exporting with strategy: format={}, quality={}, watermark={}", format, quality, watermark);

        exportStrategyFactory.getStrategy(format).execute(context, quality, watermark, customWatermarkText);
    }
}
