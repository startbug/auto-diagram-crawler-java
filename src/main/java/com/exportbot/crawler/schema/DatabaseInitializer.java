package com.exportbot.crawler.schema;

import com.exportbot.crawler.entity.SysUserEntity;
import com.exportbot.crawler.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据库表初始化器。
 * <p>
 * 应用启动时自动发现所有实现了 {@link SchemaAware} 接口的 Mapper Bean，
 * 依次调用 {@link SchemaAware#createTableIfNotExists()} 完成建表。
 * </p>
 * <p>
 * 使用 {@code CREATE TABLE IF NOT EXISTS} 语法，幂等安全，可重复执行。
 * </p>
 */
@Component
@Order(1)
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final List<SchemaAware> schemaAwareMappers;
    private final SysUserMapper sysUserMapper;

    /**
     * Spring 自动注入所有实现了 SchemaAware 接口的 Bean。
     *
     * @param schemaAwareMappers 所有 SchemaAware 实现
     * @param sysUserMapper 用户Mapper
     */
    public DatabaseInitializer(List<SchemaAware> schemaAwareMappers, SysUserMapper sysUserMapper) {
        this.schemaAwareMappers = schemaAwareMappers;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public void run(String... args) {
        if (schemaAwareMappers == null || schemaAwareMappers.isEmpty()) {
            logger.info("[Schema] 未发现任何 SchemaAware Mapper，跳过数据库初始化");
            return;
        }

        logger.info("[Schema] 开始数据库初始化，发现 {} 个表需要检查", schemaAwareMappers.size());

        int successCount = 0;
        int failCount = 0;

        for (SchemaAware mapper : schemaAwareMappers) {
            String tableName = mapper.tableName();
            try {
                mapper.createTableIfNotExists();
                successCount++;
                logger.info("[Schema] 表 [{}] 初始化完成", tableName);
            } catch (Exception e) {
                failCount++;
                logger.error("[Schema] 表 [{}] 初始化失败: {}", tableName, e.getMessage(), e);
            }
        }

        logger.info("[Schema] 数据库初始化完成: 成功={}, 失败={}, 总计={}",
                successCount, failCount, schemaAwareMappers.size());

        // 初始化默认超级管理员账号
        initDefaultAdmin();
    }

    /**
     * 初始化默认超级管理员账号
     */
    private void initDefaultAdmin() {
        try {
            // 检查是否已存在超级管理员
            SysUserEntity existingAdmin = sysUserMapper.selectByUsername("admin");
            if (existingAdmin == null) {
                SysUserEntity admin = new SysUserEntity();
                admin.setUsername("admin");
                admin.setPassword("admin123"); // 明文密码，生产环境需要加密
                admin.setNickname("超级管理员");
                admin.setRole("SUPER_ADMIN");
                admin.setStatus(1);
                sysUserMapper.insert(admin);
                logger.info("[Schema] 默认超级管理员账号已创建: username=admin, password=admin123");
            } else {
                logger.info("[Schema] 超级管理员账号已存在，跳过初始化");
            }
        } catch (Exception e) {
            logger.error("[Schema] 初始化默认管理员账号失败: {}", e.getMessage(), e);
        }
    }
}
