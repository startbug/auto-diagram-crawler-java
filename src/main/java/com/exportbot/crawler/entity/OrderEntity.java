package com.exportbot.crawler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("orders")
public class OrderEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    // === 业务字段 ===
    @TableField("order_no")
    private String orderNo;

    @TableField("item_id")
    private Long itemId;

    @TableField("order_status")
    private Integer orderStatus;

    @TableField("seller_id")
    private Long sellerId;

    @TableField("original_json")
    private String originalJson;

    @TableField("export_count")
    private Integer exportCount;

    @TableField("used_count")
    private Integer usedCount;

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
