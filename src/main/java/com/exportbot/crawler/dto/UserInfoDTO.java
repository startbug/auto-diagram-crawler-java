package com.exportbot.crawler.dto;

import lombok.Data;

/**
 * 用户信息 DTO（不返回密码）
 */
@Data
public class UserInfoDTO {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private Integer status;
}
