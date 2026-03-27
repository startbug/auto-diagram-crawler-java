package com.exportbot.crawler.engine;

import com.exportbot.crawler.config.Workflow;
import com.exportbot.crawler.config.WorkflowStep;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WorkflowRunner {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowRunner.class);

    private final StepExecutor stepExecutor;

    public WorkflowRunner(StepExecutor stepExecutor) {
        this.stepExecutor = stepExecutor;
    }

    public RunResult run(Workflow workflow, Page page, String downloadDir, Map<String, Object> externalVars) {
        long startTime = System.currentTimeMillis();
        List<String> errors = new ArrayList<>();

        logger.info("开始执行工作流: {} with {} steps", workflow.getName(), workflow.getSteps().size());

        // Merge external variables with workflow variables
        Map<String, Object> mergedVars = new java.util.HashMap<>(workflow.getVariables());
        if (externalVars != null) {
            mergedVars.putAll(externalVars);
        }

        WorkflowContext context = new WorkflowContext(page, downloadDir, mergedVars);

        for (WorkflowStep step : workflow.getSteps()) {
            try {
                logger.debug("执行步骤: {} (type: {})", step.getId(), step.getType());
                stepExecutor.execute(step, context);
            } catch (Exception e) {
                String msg = String.format("Step %s: %s", step.getId(), e.getMessage());
                errors.add(msg);
                logger.error("步骤执行失败: {}", step.getId(), e);

                if ("abort".equals(step.getOnError())) {
                    logger.warn("Workflow aborted due to error in step: {}", step.getId());
                    break;
                }
                // skip is default behavior - continue to next step
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        boolean success = errors.isEmpty();

        logger.info("Workflow finished: success={}, downloads={}, errors={}, duration={}ms",
                success, context.getDownloads().size(), errors.size(), duration);

        return new RunResult(success, context.getDownloads(), errors, duration);
    }

    public record RunResult(boolean success, List<String> downloads, List<String> errors, long duration) {}
}
