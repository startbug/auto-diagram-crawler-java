package com.exportbot.crawler.engine;

import com.exportbot.crawler.config.WorkflowStep;
import com.exportbot.crawler.engine.export.ExportStrategyFactory;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.WaitForSelectorState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class StepExecutor {

    private static final Logger logger = LoggerFactory.getLogger(StepExecutor.class);

    private final ExportStrategyFactory exportStrategyFactory;

    public StepExecutor(ExportStrategyFactory exportStrategyFactory) {
        this.exportStrategyFactory = exportStrategyFactory;
    }

    private final Random random = new Random();

    public void execute(WorkflowStep step, WorkflowContext context) throws Exception {
        String type = step.getType();

        switch (type) {
            case "navigate" -> executeNavigate(step, context);
            case "click" -> executeClick(step, context);
            case "fill" -> executeFill(step, context);
            case "select" -> executeSelect(step, context);
            case "waitForSelector" -> executeWaitForSelector(step, context);
            case "waitForTimeout" -> executeWaitForTimeout(step, context);
            case "waitForRandomTimeout" -> executeWaitForRandomTimeout(step, context);
            case "waitForResponse" -> executeWaitForResponse(step, context);
            case "evaluate" -> executeEvaluate(step, context);
            case "screenshot" -> executeScreenshot(step, context);
            case "renameDownload" -> executeRenameDownload(step, context);
            case "log" -> executeLog(step, context);
            case "loop" -> executeLoop(step, context);
            case "conditional" -> executeConditional(step, context);
            case "exportWithStrategy" -> executeExportWithStrategy(step, context);
            case "moveMouse" -> executeMoveMouse(step, context);
            case "scroll" -> executeScroll(step, context);
            case "hover" -> executeHover(step, context);
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

        // 先等待元素可见并短暂等待动画完成
        element.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        Thread.sleep(2000); // 等待2秒让元素稳定（动画完成）

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
        if (ms == null) {
            ms = 1000;
        }

        logger.info("Waiting for {} ms", ms);
        Thread.sleep(ms);
    }

    private void executeWaitForRandomTimeout(WorkflowStep step, WorkflowContext context) throws InterruptedException {
        Integer min = step.getMin() != null ? step.getMin() : 1000;
        Integer max = step.getMax() != null ? step.getMax() : 5000;
        int ms = random.nextInt(max - min + 1) + min;

        // 保存到上下文变量
        if (step.getSaveAs() != null) {
            context.setVariable(step.getSaveAs(), ms);
        }

        logger.info("Waiting random time: {} ms (min: {}, max: {})", ms, min, max);
        Thread.sleep(ms);
    }

    private void executeWaitForResponse(WorkflowStep step, WorkflowContext context) {
        String urlPattern = step.getUrlPattern() != null ? step.getUrlPattern() : step.getPattern();
        Integer timeout = step.getTimeout() != null ? step.getTimeout() : 30000;

        logger.info("Waiting for response matching: {}", urlPattern);
        Page page = context.getPage();

        // Playwright Java API: waitForResponse 使用 Runnable 回调
        Response[] responseHolder = new Response[1];
        page.waitForResponse(
                resp -> {
                    if (resp.url().contains(urlPattern)) {
                        responseHolder[0] = resp;
                        return true;
                    }
                    return false;
                },
                () -> {
                    // 等待响应期间不需要额外操作
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
        );

        Response response = responseHolder[0];
        if (response == null) {
            throw new RuntimeException("No response received matching: " + urlPattern);
        }

        logger.info("Response received: {} - {}", response.url(), response.status());

        // 解析 JSON 响应
        Map<String, Object> responseData = null;
        try {
            String text = response.text();
            responseData = new com.fasterxml.jackson.databind.ObjectMapper().readValue(text, Map.class);
        } catch (Exception e) {
            logger.warn("Failed to parse response as JSON: {}", e.getMessage());
        }

        if (step.getSaveAs() != null) {
            context.setVariable(step.getSaveAs(), responseData);
            logger.info("Response saved to variable: {}", step.getSaveAs());
        }

        // 支持 JSONPath 提取特定字段
        if (step.getJsonPath() != null && responseData != null) {
            Object extracted = extractJsonPath(responseData, step.getJsonPath());
            context.setVariable(step.getSaveAs() + "_extracted", extracted);
            logger.info("Extracted from response using '{}': {}", step.getJsonPath(), extracted);
        }
    }

    private Object extractJsonPath(Map<String, Object> obj, String path) {
        String[] parts = path.split("\\.");
        Object current = obj;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
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
        String lastDownload = context.getLastDownload();

        if (lastDownload == null) {
            throw new IllegalStateException("No download to rename");
        }

        Path sourcePath = Paths.get(lastDownload);
        String extension = getFileExtension(lastDownload);
        String newFilename = generateNewFilename(extension);
        Path targetPath = Paths.get(context.getDownloadDir(), newFilename);

        try {
            java.nio.file.Files.move(sourcePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            context.addDownload(targetPath.toString());
            logger.info("Renamed download: {} -> {}", sourcePath, targetPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to rename download", e);
        }
    }

    /**
     * 获取文件扩展名（包含点号）
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return ""; // 没有扩展名
    }

    /**
     * 生成新文件名：毫秒时间戳_UUID + 扩展名
     */
    private String generateNewFilename(String extension) {
        long timestamp = System.currentTimeMillis();
        String uuid = java.util.UUID.randomUUID().toString();
        return timestamp + "_" + uuid + extension;
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

    private void executeMoveMouse(WorkflowStep step, WorkflowContext context) throws InterruptedException {
        String selector = context.interpolate(step.getSelector());
        Page page = context.getPage();

        Locator locator = page.locator(selector);
        if (locator.count() == 0) {
            logger.warn("Element not found for mouse move: {}", selector);
            return;
        }

        // 获取元素位置
        com.microsoft.playwright.JSHandle handle = locator.evaluateHandle(
                "el => ({ x: el.getBoundingClientRect().x, y: el.getBoundingClientRect().y, width: el.getBoundingClientRect().width, height: el.getBoundingClientRect().height })");
        Map<String, Object> box = (Map<String, Object>) handle.jsonValue();

        if (box == null) {
            logger.warn("Element has no bounding box: {}", selector);
            return;
        }

        double x = ((Number) box.get("x")).doubleValue();
        double y = ((Number) box.get("y")).doubleValue();
        double width = ((Number) box.get("width")).doubleValue();
        double height = ((Number) box.get("height")).doubleValue();

        // 随机偏移，不精确对准中心
        double offsetX = (random.nextDouble() - 0.5) * 20;
        double offsetY = (random.nextDouble() - 0.5) * 20;
        double targetX = x + width / 2 + offsetX;
        double targetY = y + height / 2 + offsetY;

        // 分多步移动，模拟真人轨迹
        int steps = random.nextInt(6) + 3; // 3-8步

        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            double nextX = targetX * progress + (random.nextDouble() - 0.5) * 50;
            double nextY = targetY * progress + (random.nextDouble() - 0.5) * 50;
            page.mouse().move(nextX, nextY);
            Thread.sleep((long) (random.nextDouble() * 100 + 50));
        }

        logger.info("Mouse moved to: {} (selector: {}, steps: {})", targetX + "," + targetY, selector, steps);
    }

    private void executeScroll(WorkflowStep step, WorkflowContext context) throws InterruptedException {
        String direction = step.getDirection() != null ? step.getDirection() : "down";
        Integer amount = step.getAmount() != null ? step.getAmount() : 300;

        // 随机分多次滚动
        int steps = random.nextInt(3) + 2;
        int stepAmount = amount / steps;

        Page page = context.getPage();
        for (int i = 0; i < steps; i++) {
            int scrollAmt = "down".equals(direction) ? stepAmount : -stepAmount;
            try {
                page.evaluate("window.scrollBy(0, " + scrollAmt + ")");
            } catch (com.microsoft.playwright.PlaywrightException e) {
                // 页面导航导致执行上下文被销毁，这是正常现象
                if (e.getMessage().contains("Execution context was destroyed") ||
                        e.getMessage().contains("navigation")) {
                    logger.warn("Scroll interrupted due to page navigation, continuing...");
                    break;
                }
                throw e;
            }
            Thread.sleep((long) (random.nextDouble() * 200 + 100));
        }

        logger.info("Scrolled {} by {} pixels ({} steps)", direction, amount, steps);
    }

    private void executeHover(WorkflowStep step, WorkflowContext context) {
        String selector = context.interpolate(step.getSelector());
        logger.info("Hovering element: {}", selector);
        Page page = context.getPage();
        page.locator(selector).hover();
    }
}
