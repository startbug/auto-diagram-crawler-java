package com.exportbot.crawler.config;

import com.exportbot.crawler.mapper.WorkflowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据库表结构更新器
 * 应用启动时自动检查并更新表结构
 */
@Component
public class DatabaseSchemaUpdater implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaUpdater.class);

    private final WorkflowMapper workflowMapper;

    public DatabaseSchemaUpdater(WorkflowMapper workflowMapper) {
        this.workflowMapper = workflowMapper;
    }

    @Override
    public void run(String... args) {
        logger.info("开始检查并更新数据库表结构...");
        
        try {
            // 检查并添加新字段
            checkAndAddColumns();
            
            // 检查并重命名字段
            checkAndRenameColumns();
            
            // 检查并添加索引
            checkAndAddIndexes();
            
            // 更新字段备注
            updateColumnComments();
            
            // 更新表备注
            updateTableComment();
            
            logger.info("数据库表结构更新完成");
        } catch (Exception e) {
            logger.error("数据库表结构更新失败: {}", e.getMessage(), e);
        }
    }

    private void checkAndAddColumns() {
        try {
            // 添加 creator 字段
            workflowMapper.addColumnIfNotExists("creator", "VARCHAR(64)", "创建人");
            logger.info("字段 creator 检查完成");
        } catch (Exception e) {
            logger.debug("字段 creator 可能已存在: {}", e.getMessage());
        }

        try {
            // 添加 modifier 字段
            workflowMapper.addColumnIfNotExists("modifier", "VARCHAR(64)", "修改人");
            logger.info("字段 modifier 检查完成");
        } catch (Exception e) {
            logger.debug("字段 modifier 可能已存在: {}", e.getMessage());
        }

        try {
            // 添加 deleted 字段
            workflowMapper.addColumnIfNotExists("deleted", "TINYINT DEFAULT 0", "逻辑删除：0-未删除，1-已删除");
            logger.info("字段 deleted 检查完成");
        } catch (Exception e) {
            logger.debug("字段 deleted 可能已存在: {}", e.getMessage());
        }
    }

    private void checkAndRenameColumns() {
        try {
            // 重命名 created_at -> create_time
            workflowMapper.renameColumnIfExists("created_at", "create_time", "TIMESTAMP", "创建时间");
            logger.info("字段 created_at -> create_time 重命名完成");
        } catch (Exception e) {
            logger.debug("字段 created_at 重命名可能已完成或不存在: {}", e.getMessage());
        }

        try {
            // 重命名 updated_at -> modify_time
            workflowMapper.renameColumnIfExists("updated_at", "modify_time", "TIMESTAMP", "修改时间");
            logger.info("字段 updated_at -> modify_time 重命名完成");
        } catch (Exception e) {
            logger.debug("字段 updated_at 重命名可能已完成或不存在: {}", e.getMessage());
        }
    }

    private void checkAndAddIndexes() {
        try {
            workflowMapper.addIndexIfNotExists("idx_create_time", "create_time");
            logger.info("索引 idx_create_time 检查完成");
        } catch (Exception e) {
            logger.debug("索引 idx_create_time 可能已存在: {}", e.getMessage());
        }

        try {
            workflowMapper.addIndexIfNotExists("idx_modify_time", "modify_time");
            logger.info("索引 idx_modify_time 检查完成");
        } catch (Exception e) {
            logger.debug("索引 idx_modify_time 可能已存在: {}", e.getMessage());
        }

        try {
            workflowMapper.addIndexIfNotExists("idx_deleted", "deleted");
            logger.info("索引 idx_deleted 检查完成");
        } catch (Exception e) {
            logger.debug("索引 idx_deleted 可能已存在: {}", e.getMessage());
        }
    }

    private void updateColumnComments() {
        try {
            workflowMapper.updateColumnComment("id", "主键ID");
            workflowMapper.updateColumnComment("code", "工作流编码，唯一标识");
            workflowMapper.updateColumnComment("name", "工作流名称");
            workflowMapper.updateColumnComment("description", "工作流描述");
            workflowMapper.updateColumnComment("content", "工作流内容（YAML格式）");
            logger.info("字段备注更新完成");
        } catch (Exception e) {
            logger.warn("字段备注更新失败: {}", e.getMessage());
        }
    }

    private void updateTableComment() {
        try {
            workflowMapper.updateTableComment("工作流配置表");
            logger.info("表备注更新完成");
        } catch (Exception e) {
            logger.warn("表备注更新失败: {}", e.getMessage());
        }
    }
}
