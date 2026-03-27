package com.exportbot.crawler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 交付插件类型枚举
 */
@Getter
@AllArgsConstructor
public enum DeliveryPluginType {

    SMTP("smtp", "SMTP邮件发送"),
    ALIYUN_OSS("aliyun-oss", "阿里云OSS存储");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static Optional<DeliveryPluginType> getByCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.getCode().equalsIgnoreCase(code))
                .findFirst();
    }
}
