CREATE TABLE `workflows`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code`        varchar(64)  NOT NULL COMMENT '工作流编码，唯一标识',
    `name`        varchar(255) NOT NULL COMMENT '工作流名称',
    `description` text        DEFAULT NULL COMMENT '工作流描述',
    `content`     text         NOT NULL COMMENT '工作流内容（YAML格式）',
    `create_time` timestamp   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time` timestamp   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator`     varchar(64) DEFAULT NULL COMMENT '创建人',
    `modifier`    varchar(64) DEFAULT NULL COMMENT '修改人',
    `deleted`     tinyint     DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`) /*T![clustered_index] CLUSTERED */,
    KEY `idx_code` (`code`) COMMENT '编码索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引',
    KEY `idx_modify_time` (`modify_time`) COMMENT '修改时间索引',
    KEY `idx_deleted` (`deleted`) COMMENT '逻辑删除索引',
    UNIQUE KEY `code` (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT ='工作流配置表'
