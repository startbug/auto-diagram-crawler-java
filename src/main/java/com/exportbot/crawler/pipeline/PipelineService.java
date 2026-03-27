package com.exportbot.crawler.pipeline;

import com.exportbot.crawler.auth.AuthData;
import com.exportbot.crawler.auth.AuthInjector;
import com.exportbot.crawler.auth.AuthStore;
import com.exportbot.crawler.browser.BrowserFactory;
import com.exportbot.crawler.config.AppConfig;
import com.exportbot.crawler.config.ConfigLoader;
import com.exportbot.crawler.config.Workflow;
import com.exportbot.crawler.delivery.DeliveryManager;
import com.exportbot.crawler.delivery.DeliveryPlugin;
import com.exportbot.crawler.engine.WorkflowRunner;
import com.exportbot.crawler.repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class PipelineService {

    private static final Logger logger = LoggerFactory.getLogger(PipelineService.class);

    private final AppConfig config;
    private final ConfigLoader configLoader;
    private final AuthStore authStore;
    private final AuthInjector authInjector;
    private final BrowserFactory browserFactory;
    private final WorkflowRunner workflowRunner;
    private final DeliveryManager deliveryManager;
    private final WorkflowRepository workflowRepository;

    private volatile boolean running = false;

    public PipelineService(AppConfig config, ConfigLoader configLoader,
                          AuthStore authStore, AuthInjector authInjector,
                          BrowserFactory browserFactory, WorkflowRunner workflowRunner,
                          DeliveryManager deliveryManager, WorkflowRepository workflowRepository) {
        this.config = config;
        this.configLoader = configLoader;
        this.authStore = authStore;
        this.authInjector = authInjector;
        this.browserFactory = browserFactory;
        this.workflowRunner = workflowRunner;
        this.deliveryManager = deliveryManager;
        this.workflowRepository = workflowRepository;
    }

    public PipelineResult runPipeline(String workflowFile, boolean skipDelivery, Map<String, Object> variables) {
        if (running) {
            throw new IllegalStateException("A pipeline run is already in progress");
        }
        running = true;

        long startTime = System.currentTimeMillis();
        try {
            // Load workflow from database or file
            Workflow workflow;
            if (workflowFile != null && !workflowFile.contains("/") && !workflowFile.contains("\\") && !workflowFile.endsWith(".yaml") && !workflowFile.endsWith(".yml")) {
                // It's a workflow ID, load from database
                workflow = workflowRepository.loadWorkflow(workflowFile);
            } else {
                // It's a file path, load from file
                String workflowPath = workflowFile != null ? workflowFile : config.getWorkflow().getFile();
                workflow = configLoader.loadWorkflow(workflowPath);
            }

            // Ensure download directory
            // TODO 从yml中读取路径，发到线上服务器后指定的是绝对路径
            String downloadDir = Paths.get(config.getBrowser().getDownloadDir()).toAbsolutePath().toString();
            java.io.File dir = new java.io.File(downloadDir);
            dir.mkdirs();

            // Load auth
            // TODO 从yml中读取路径，发到线上服务器后指定的是绝对路径
            AuthData auth = authStore.load(config.getAuth().getStorePath());

            // Launch browser
            var browserInstance = browserFactory.createBrowser(config);

            WorkflowRunner.RunResult workflowResult;
            try {
                // Inject auth
                String targetUrl = workflow.getVariables().getOrDefault("baseUrl", "").toString();
                authInjector.injectAuth(browserInstance.context(), browserInstance.page(), auth, targetUrl);

                // Run workflow
                workflowResult = workflowRunner.run(workflow, browserInstance.page(), downloadDir, variables);
            } finally {
                browserInstance.close();
            }

            // Deliver files
            List<DeliveryPlugin.DeliveryResult> deliveryResults = List.of();
            if (!skipDelivery && !workflowResult.downloads().isEmpty()) {
                deliveryManager.initialize(config.getDelivery());
                deliveryResults = deliveryManager.deliverAll(
                        workflowResult.downloads(),
                        new DeliveryPlugin.DeliveryMetadata(
                                workflow.getName(),
                                LocalDate.now().toString(),
                                workflow.getVariables()
                        )
                );
            }

            long totalDuration = System.currentTimeMillis() - startTime;
            logger.info("Pipeline complete: duration={}ms, success={}", totalDuration, workflowResult.success());

            return new PipelineResult(workflowResult, deliveryResults, totalDuration);

        } finally {
            running = false;
        }
    }

    public record PipelineResult(
            WorkflowRunner.RunResult workflow,
            List<DeliveryPlugin.DeliveryResult> delivery,
            long totalDuration
    ) {}
}
