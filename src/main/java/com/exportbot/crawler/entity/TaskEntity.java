package com.exportbot.crawler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tasks")
public class TaskEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    // === 业务字段 ===
    @TableField("order_id")
    private Long orderId;

    private String uuid;

    private String email;

    @TableField("user_ip")
    private String userIp;

    @TableField("file_url")
    private String fileUrl;

    private String format;

    private String quality;

    @TableField("watermark_type")
    private String watermarkType;

    @TableField("watermark_text")
    private String watermarkText;

    private Integer status;

    @TableField("oss_url")
    private String ossUrl;

    @TableField("reset_count")
    private Integer resetCount;

    @TableField("error_message")
    private String errorMessage;

    // === 标准字段（所有表必须包含） ===
    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("creator")
    private String creator;

    @TableField("modifier")
    private String modifier;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
