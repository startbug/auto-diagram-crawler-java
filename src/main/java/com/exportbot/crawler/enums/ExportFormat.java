package com.exportbot.crawler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 导出格式枚举
 */
@Getter
@AllArgsConstructor
public enum ExportFormat {

    JPG("jpg", "JPG图片格式"),
    PNG("png", "PNG图片格式"),
    PDF("pdf", "PDF文档格式"),
    SVG("svg", "SVG矢量格式"),
    VISIO("visio", "Visio格式"),
    POS("pos", "POS格式");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static Optional<ExportFormat> getByCode(String code) {
        return Arrays.stream(values())
                .filter(format -> format.getCode().equalsIgnoreCase(code))
                .findFirst();
    }
}
