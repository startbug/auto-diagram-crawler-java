package com.exportbot.crawler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 工作流步骤类型枚举
 */
@Getter
@AllArgsConstructor
public enum StepType {

    NAVIGATE("navigate", "页面导航"),
    CLICK("click", "点击元素"),
    FILL("fill", "填充输入"),
    SELECT("select", "选择选项"),
    WAIT_FOR_SELECTOR("waitForSelector", "等待元素"),
    WAIT_FOR_TIMEOUT("waitForTimeout", "固定等待"),
    WAIT_FOR_RANDOM_TIMEOUT("waitForRandomTimeout", "随机等待"),
    WAIT_FOR_RESPONSE("waitForResponse", "等待响应"),
    EVALUATE("evaluate", "执行脚本"),
    SCREENSHOT("screenshot", "截图"),
    RENAME_DOWNLOAD("renameDownload", "重命名下载文件"),
    LOG("log", "日志记录"),
    LOOP("loop", "循环执行"),
    CONDITIONAL("conditional", "条件执行"),
    EXPORT_WITH_STRATEGY("exportWithStrategy", "策略导出"),
    MOVE_MOUSE("moveMouse", "移动鼠标"),
    SCROLL("scroll", "页面滚动"),
    HOVER("hover", "悬停元素");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static Optional<StepType> getByCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.getCode().equalsIgnoreCase(code))
                .findFirst();
    }
}
