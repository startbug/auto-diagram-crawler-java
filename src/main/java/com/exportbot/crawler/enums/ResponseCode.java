package com.exportbot.crawler.enums;

import lombok.Getter;

/**
 * @Author starbug
 * @Description
 * @Datetime 2026/3/27 23:26
 */
@Getter
public enum ResponseCode {

    SUCCESS(200, "成功"),

    FAIL(500, "失败"),

    ;

    private int code;

    private String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
