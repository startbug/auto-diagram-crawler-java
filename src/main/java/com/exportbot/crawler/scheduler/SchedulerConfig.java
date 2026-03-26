package com.exportbot.crawler.scheduler;

import com.exportbot.crawler.config.AppConfig;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    @Bean
    public JobDetail exportJobDetail() {
        return JobBuilder.newJob(ExportJob.class)
                .withIdentity("exportJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger exportJobTrigger(AppConfig config) {
        if (!config.getSchedule().isEnabled()) {
            logger.info("Scheduler is disabled");
            return null;
        }

        String cronExpression = config.getSchedule().getCron();
        logger.info("Scheduling export job with cron: {}", cronExpression);

        return TriggerBuilder.newTrigger()
                .forJob(exportJobDetail())
                .withIdentity("exportTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }
}
