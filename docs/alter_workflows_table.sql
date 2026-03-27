-- workflows 表结构变更脚本
-- 执行时间: 2025-03-27
-- 目的: 添加创建人、修改人字段，重命名时间字段，添加字段备注和索引

-- 1. 重命名时间字段
ALTER TABLE workflows 
    CHANGE COLUMN created_at create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CHANGE COLUMN updated_at modify_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间';

-- 2. 添加创建人、修改人字段
ALTER TABLE workflows 
    ADD COLUMN creator VARCHAR(64) DEFAULT NULL COMMENT '创建人' AFTER modify_time,
    ADD COLUMN modifier VARCHAR(64) DEFAULT NULL COMMENT '修改人' AFTER creator;

-- 3. 添加逻辑删除字段（如果不存在）
ALTER TABLE workflows 
    ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除' AFTER modifier;

-- 4. 添加索引
ALTER TABLE workflows 
    ADD INDEX idx_create_time (create_time) COMMENT '创建时间索引',
    ADD INDEX idx_modify_time (modify_time) COMMENT '修改时间索引',
    ADD INDEX idx_deleted (deleted) COMMENT '逻辑删除索引';

-- 5. 添加字段备注（针对已有字段）
ALTER TABLE workflows 
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    MODIFY COLUMN code VARCHAR(64) NOT NULL COMMENT '工作流编码，唯一标识',
    MODIFY COLUMN name VARCHAR(255) NOT NULL COMMENT '工作流名称',
    MODIFY COLUMN description TEXT COMMENT '工作流描述',
    MODIFY COLUMN content TEXT NOT NULL COMMENT '工作流内容（YAML格式）';

-- 6. 添加表备注
ALTER TABLE workflows COMMENT='工作流配置表';
