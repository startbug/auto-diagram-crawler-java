package com.exportbot.crawler.dto;

import lombok.Data;

/**
 * 定时任务配置请求 DTO
 */
@Data
public class SchedulerConfigRequestDTO {
    private String jobName;
    private String cronExpression;
    private Integer enabled;
    private String description;
}
