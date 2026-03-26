package com.exportbot.crawler.cli;

import com.exportbot.crawler.auth.AuthData;
import com.exportbot.crawler.auth.AuthInjector;
import com.exportbot.crawler.auth.AuthStore;
import com.exportbot.crawler.browser.BrowserFactory;
import com.exportbot.crawler.config.AppConfig;
import com.exportbot.crawler.config.ConfigLoader;
import com.exportbot.crawler.config.Workflow;
import com.exportbot.crawler.delivery.DeliveryManager;
import com.exportbot.crawler.delivery.DeliveryPlugin;
import com.exportbot.crawler.pipeline.PipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "crawler", mixinStandardHelpOptions = true, version = "1.0.0",
        description = "Browser export automation tool")
public class CliCommands implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CliCommands.class);

    private final PipelineService pipelineService;
    private final ConfigLoader configLoader;
    private final AuthStore authStore;
    private final AuthInjector authInjector;
    private final BrowserFactory browserFactory;
    private final AppConfig config;
    private final DeliveryManager deliveryManager;

    public CliCommands(PipelineService pipelineService, ConfigLoader configLoader,
                      AuthStore authStore, AuthInjector authInjector,
                      BrowserFactory browserFactory, AppConfig config,
                      DeliveryManager deliveryManager) {
        this.pipelineService = pipelineService;
        this.configLoader = configLoader;
        this.authStore = authStore;
        this.authInjector = authInjector;
        this.browserFactory = browserFactory;
        this.config = config;
        this.deliveryManager = deliveryManager;
    }

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            // No CLI args, let Spring Boot run as web server
            return;
        }

        CommandLine cmd = new CommandLine(this);
        cmd.addSubcommand("run", new RunCommand());
        cmd.addSubcommand("validate", new ValidateCommand());
        cmd.addSubcommand("test-auth", new TestAuthCommand());
        cmd.addSubcommand("test-delivery", new TestDeliveryCommand());

        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

    @CommandLine.Command(name = "run", description = "Execute the export workflow")
    class RunCommand implements Callable<Integer> {
        @CommandLine.Option(names = {"-w", "--workflow"}, description = "Path to workflow YAML file")
        String workflow;

        @CommandLine.Option(names = {"--skip-delivery"}, description = "Skip file delivery after export")
        boolean skipDelivery;

        @CommandLine.Option(names = {"--url"}, description = "Target URL to export")
        String url;

        @CommandLine.Option(names = {"--password"}, description = "Password for accessing the shared link")
        String password;

        @CommandLine.Option(names = {"--format"}, description = "Export format: png, jpg, pdf, svg")
        String format;

        @CommandLine.Option(names = {"--quality"}, description = "Image quality: hd, normal")
        String quality;

        @CommandLine.Option(names = {"--watermark"}, description = "Watermark type: system, none, custom")
        String watermark;

        @Override
        public Integer call() {
            try {
                Map<String, Object> variables = new HashMap<>();
                if (url != null) variables.put("url", url);
                if (password != null) variables.put("password", password);
                if (format != null) variables.put("format", format);
                if (quality != null) variables.put("quality", quality);
                if (watermark != null) variables.put("watermark", watermark);

                var result = pipelineService.runPipeline(workflow, skipDelivery, variables);

                System.out.println("=== Pipeline Result ===");
                System.out.println("Workflow Success: " + result.workflow().success());
                System.out.println("Files Downloaded: " + result.workflow().downloads().size());
                System.out.println("Delivery Results:");
                for (var dr : result.delivery()) {
                    System.out.println("  " + dr.pluginName() + ": " + (dr.success() ? "OK" : dr.message()));
                }
                System.out.println("Total Duration: " + result.totalDuration() + "ms");

                return result.workflow().success() ? 0 : 1;
            } catch (Exception e) {
                logger.error("Pipeline failed", e);
                return 1;
            }
        }
    }

    @CommandLine.Command(name = "validate", description = "Validate workflow YAML file")
    class ValidateCommand implements Callable<Integer> {
        @CommandLine.Option(names = {"-w", "--workflow"}, description = "Path to workflow YAML file")
        String workflow;

        @Override
        public Integer call() {
            try {
                String workflowPath = workflow != null ? workflow : config.getWorkflow().getFile();
                Workflow w = configLoader.loadWorkflow(workflowPath);

                System.out.println("Workflow is valid:");
                System.out.println("  Name: " + w.getName());
                System.out.println("  Steps: " + w.getSteps().size());
                System.out.println("  Variables: " + w.getVariables().keySet());
                return 0;
            } catch (Exception e) {
                System.err.println("Validation failed: " + e.getMessage());
                return 1;
            }
        }
    }

    @CommandLine.Command(name = "test-auth", description = "Test authentication by navigating to target site")
    class TestAuthCommand implements Callable<Integer> {
        @CommandLine.Option(names = {"-u", "--url"}, description = "URL to navigate to for testing")
        String testUrl;

        @Override
        public Integer call() {
            try {
                AuthData auth = authStore.load(config.getAuth().getStorePath());
                if (auth.getCookies().isEmpty() && auth.getToken() == null) {
                    System.err.println("No auth configured. Please set up auth first via the web UI.");
                    return 1;
                }

                var browser = browserFactory.createBrowser(config);
                try {
                    Workflow workflow = configLoader.loadWorkflow(config.getWorkflow().getFile());
                    String url = testUrl != null ? testUrl : workflow.getVariables().getOrDefault("baseUrl", "https://example.com").toString();

                    authInjector.injectAuth(browser.context(), browser.page(), auth, url);

                    System.out.println("Testing auth... URL: " + url);
                    browser.page().navigate(url);

                    String title = browser.page().title();
                    System.out.println("Page loaded. Title: " + title);
                    System.out.println("Auth appears to be working.");
                    return 0;
                } finally {
                    browser.close();
                }
            } catch (Exception e) {
                logger.error("Auth test failed", e);
                return 1;
            }
        }
    }

    @CommandLine.Command(name = "test-delivery", description = "Test delivery plugins with a dummy file")
    class TestDeliveryCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            try {
                // Create test file
                String testDir = config.getBrowser().getDownloadDir();
                Path testDirPath = Paths.get(testDir);
                testDirPath.toFile().mkdirs();

                Path testFile = testDirPath.resolve("test-delivery.txt");
                Files.writeString(testFile, "Test delivery file\nGenerated at: " + java.time.Instant.now() + "\n");

                deliveryManager.initialize(config.getDelivery());

                var results = deliveryManager.deliverAll(
                        java.util.List.of(testFile.toString()),
                        new DeliveryPlugin.DeliveryMetadata("test", LocalDate.now().toString(), Map.of())
                );

                for (var r : results) {
                    if (r.success()) {
                        System.out.println(r.pluginName() + ": OK - " + r.message());
                    } else {
                        System.err.println(r.pluginName() + ": FAILED - " + r.message());
                    }
                }

                // Cleanup
                Files.deleteIfExists(testFile);
                return 0;
            } catch (Exception e) {
                logger.error("Delivery test failed", e);
                return 1;
            }
        }
    }
}
