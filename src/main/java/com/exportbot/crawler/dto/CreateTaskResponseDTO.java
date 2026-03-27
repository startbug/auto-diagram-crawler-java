package com.exportbot.crawler.dto;

import lombok.Data;

/**
 * 任务创建响应 DTO
 */
@Data
public class CreateTaskResponseDTO {
    private Long taskId;
    private String uuid;
}
