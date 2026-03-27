package com.exportbot.crawler.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {

    PENDING(0, "待执行"),
    RUNNING(100, "执行中"),
    SUCCESS(200, "执行成功"),
    FAILED(300, "执行异常"),
    DELIVERING(400, "待交付"),
    DELIVERED(500, "已交付");

    private final int code;
    private final String desc;

    TaskStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TaskStatus fromCode(int code) {
        for (TaskStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING;
    }
}
