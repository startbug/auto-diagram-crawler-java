package com.exportbot.crawler.scheduler;

import com.exportbot.crawler.config.AppConfig;
import com.exportbot.crawler.pipeline.PipelineService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExportJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ExportJob.class);

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private AppConfig config;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Scheduled export job started");

        try {
            var result = pipelineService.runPipeline(null, false, null);

            if (result.workflow().success()) {
                logger.info("Scheduled export completed successfully: {} files downloaded",
                        result.workflow().downloads().size());
            } else {
                logger.error("Scheduled export failed: {}", result.workflow().errors());
                throw new JobExecutionException("Export workflow failed");
            }
        } catch (Exception e) {
            logger.error("Scheduled export job failed", e);
            throw new JobExecutionException(e);
        }
    }
}
