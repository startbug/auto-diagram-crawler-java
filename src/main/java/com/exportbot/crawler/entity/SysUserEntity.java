package com.exportbot.crawler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_users")
public class SysUserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    // === 业务字段 ===
    private String username;
    private String password;
    private String nickname;
    private String role;
    private Integer status;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

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
