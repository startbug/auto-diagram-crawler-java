package com.exportbot.crawler.dto;

import lombok.Data;

/**
 * 工作流保存请求 DTO
 */
@Data
public class SaveWorkflowRequestDTO {
    private String code;
    private String name;
    private String description;
    private String content;
}
