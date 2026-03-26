package com.exportbot.crawler.config;

import com.exportbot.crawler.repository.WorkflowRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final WorkflowRepository workflowRepository;

    public DataInitializer(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    @Override
    public void run(String... args) {
        // Check if processon workflow exists
        if (workflowRepository.findByCode("processon").isEmpty()) {
            logger.info("Initializing default workflow: processon");

            // Try to load from classpath (resources directory)
            ClassPathResource resource = new ClassPathResource("config/processon.yaml");

            if (resource.exists()) {
                try {
                    String content = StreamUtils.copyToString(resource.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
                    workflowRepository.save(
                            "processon",
                            "Processon 动态导出流程",
                            "ProcessOn 自动转存 + 动态格式导出工作流（拟人化操作）",
                            content
                    );
                    logger.info("Workflow 'processon' initialized from classpath");
                } catch (IOException e) {
                    logger.error("Failed to load workflow from classpath", e);
                }
            } else {
                logger.warn("Workflow file not found in classpath, skipping initialization");
            }
        } else {
            logger.info("Workflow 'processon' already exists");
        }
    }
}
