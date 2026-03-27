package com.exportbot.crawler.dto;

import lombok.Data;

/**
 * OSS URL 响应 DTO
 */
@Data
public class OssUrlResponseDTO {
    private String ossUrl;
    private Integer expireHours;
}
