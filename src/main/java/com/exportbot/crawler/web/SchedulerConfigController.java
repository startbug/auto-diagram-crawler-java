package com.exportbot.crawler.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exportbot.crawler.dto.SchedulerConfigRequestDTO;
import com.exportbot.crawler.entity.SchedulerConfigEntity;
import com.exportbot.crawler.entity.common.R;
import com.exportbot.crawler.repository.SchedulerConfigRepository;
import com.exportbot.crawler.scheduler.ExportJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduler/configs")
public class SchedulerConfigController {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfigController.class);

    private final SchedulerConfigRepository schedulerConfigRepository;
    private final Scheduler scheduler;

    public SchedulerConfigController(SchedulerConfigRepository schedulerConfigRepository, Scheduler scheduler) {
        this.schedulerConfigRepository = schedulerConfigRepository;
        this.scheduler = scheduler;
    }

    @GetMapping
    public ResponseEntity<R<IPage<SchedulerConfigEntity>>> listConfigs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String jobName) {
        return ResponseEntity.ok(R.success(schedulerConfigRepository.findPage(pageNum, pageSize, jobName)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<R<SchedulerConfigEntity>> getConfig(@PathVariable Long id) {
        return schedulerConfigRepository.findById(id)
                .map(entity -> ResponseEntity.ok(R.success(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<R<SchedulerConfigEntity>> createConfig(@RequestBody SchedulerConfigRequestDTO request) {
        try {
            // 校验 Cron 表达式
            if (!CronExpression.isValidExpression(request.getCronExpression())) {
                return ResponseEntity.badRequest()
                        .body(R.error("无效的 Cron 表达式"));
            }

            // 检查任务名称是否已存在
            if (schedulerConfigRepository.findByJobName(request.getJobName()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(R.error("任务名称已存在"));
            }

            SchedulerConfigEntity config = new SchedulerConfigEntity();
            config.setJobName(request.getJobName());
            config.setCronExpression(request.getCronExpression());
            config.setEnabled(request.getEnabled() != null ? request.getEnabled() : 1);
            config.setDescription(request.getDescription());

            schedulerConfigRepository.save(config);

            // 如果启用，则创建 Quartz 任务
            if (config.getEnabled() == 1) {
                createOrUpdateQuartzJob(config);
            }
            
            return ResponseEntity.ok(R.success(config));
        } catch (Exception e) {
            logger.error("Failed to create scheduler config", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<R<Void>> updateConfig(@PathVariable Long id, @RequestBody SchedulerConfigRequestDTO request) {
        try {
            SchedulerConfigEntity config = schedulerConfigRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("配置不存在"));

            // 校验 Cron 表达式
            if (request.getCronExpression() != null && !CronExpression.isValidExpression(request.getCronExpression())) {
                return ResponseEntity.badRequest()
                        .body(R.error("无效的 Cron 表达式"));
            }

            if (request.getCronExpression() != null) {
                config.setCronExpression(request.getCronExpression());
            }
            if (request.getEnabled() != null) {
                config.setEnabled(request.getEnabled());
            }
            if (request.getDescription() != null) {
                config.setDescription(request.getDescription());
            }

            schedulerConfigRepository.save(config);

            // 更新 Quartz 任务
            createOrUpdateQuartzJob(config);

            return ResponseEntity.ok(R.success(null));
        } catch (RuntimeException e) {
            logger.warn("Failed to update scheduler config: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(R.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to update scheduler config", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<R<Void>> deleteConfig(@PathVariable Long id) {
        try {
            SchedulerConfigEntity config = schedulerConfigRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("配置不存在"));

            // 删除 Quartz 任务
            deleteQuartzJob(config.getJobName());

            schedulerConfigRepository.deleteById(id);

            return ResponseEntity.ok(R.success(null));
        } catch (RuntimeException e) {
            logger.warn("Failed to delete scheduler config: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(R.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to delete scheduler config", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/trigger")
    public ResponseEntity<R<Void>> triggerJob(@PathVariable Long id) {
        try {
            SchedulerConfigEntity config = schedulerConfigRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("配置不存在"));

            // 立即触发任务
            JobKey jobKey = JobKey.jobKey(config.getJobName());
            if (scheduler.checkExists(jobKey)) {
                scheduler.triggerJob(jobKey);
                logger.info("Job triggered manually: {}", config.getJobName());
                return ResponseEntity.ok(R.success(null));
            } else {
                return ResponseEntity.badRequest()
                        .body(R.error("任务不存在，请先启用配置"));
            }
        } catch (RuntimeException e) {
            logger.warn("Failed to trigger job: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(R.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to trigger job", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    private void createOrUpdateQuartzJob(SchedulerConfigEntity config) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(config.getJobName());
        TriggerKey triggerKey = TriggerKey.triggerKey(config.getJobName() + "_trigger");

        // 删除旧任务
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        // 如果禁用，则不创建新任务
        if (config.getEnabled() == 0) {
            logger.info("Job disabled: {}", config.getJobName());
            return;
        }

        // 创建新任务
        JobDetail jobDetail = JobBuilder.newJob(ExportJob.class)
                .withIdentity(jobKey)
                .storeDurably()
                .build();

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(config.getCronExpression());

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(scheduleBuilder)
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        logger.info("Job scheduled: {}, cron: {}", config.getJobName(), config.getCronExpression());
    }

    private void deleteQuartzJob(String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            logger.info("Job deleted: {}", jobName);
        }
    }


}
