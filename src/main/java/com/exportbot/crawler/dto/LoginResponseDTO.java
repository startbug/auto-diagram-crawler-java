package com.exportbot.crawler.dto;

import lombok.Data;

/**
 * 登录响应 DTO
 */
@Data
public class LoginResponseDTO {
    private String token;
    private UserInfoDTO userInfo;
}
