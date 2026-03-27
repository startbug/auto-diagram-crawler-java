package com.exportbot.crawler.dto;

import lombok.Data;

/**
 * 订单回调请求 DTO
 */
@Data
public class OrderCallbackRequestDTO {
    private Long bizOrderId;
    private Integer itemId;
    private Integer orderStatus;
    private Long sellerId;
}
