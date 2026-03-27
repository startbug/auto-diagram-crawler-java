package com.exportbot.crawler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scheduler_configs")
public class SchedulerConfigEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    // === 业务字段 ===
    @TableField("job_name")
    private String jobName;

    @TableField("cron_expression")
    private String cronExpression;

    private Integer enabled;

    private String description;

    @TableField("last_execute_time")
    private LocalDateTime lastExecuteTime;

    @TableField("last_execute_result")
    private String lastExecuteResult;

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
