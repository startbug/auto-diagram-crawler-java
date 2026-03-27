package com.exportbot.crawler.dto;

import lombok.Data;

/**
 * 任务创建请求 DTO
 */
@Data
public class CreateTaskRequestDTO {
    private Long orderId;
    private String email;
    private String fileUrl;
    private String format;
    private String quality;
    private String watermarkType;
    private String watermarkText;
    
    // C 端接口字段
    private String orderUuid;
    private Integer timestamp;
    private String nonce;
    private String signature;
}
