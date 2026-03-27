package com.exportbot.crawler.schema;

/**
 * 数据库表 Schema 感知接口。
 * <p>
 * 所有需要自动建表的 Mapper 必须实现此接口。
 * DatabaseInitializer 在应用启动时会自动发现并调用 {@link #createTableIfNotExists()}。
 * </p>
 *
 * <h3>使用方式</h3>
 * <ol>
 *   <li>Mapper 接口继承 {@code SchemaAware}</li>
 *   <li>在对应的 Mapper XML 中编写 {@code createTableIfNotExists} 的 SQL（CREATE TABLE IF NOT EXISTS ...）</li>
 *   <li>实现 {@link #tableName()} 返回表名（用于日志输出）</li>
 * </ol>
 */
public interface SchemaAware {

    /**
     * 创建表（如果不存在）。
     * <p>
     * 对应 Mapper XML 中的 {@code <update id="createTableIfNotExists">} 语句。
     * 必须使用 {@code CREATE TABLE IF NOT EXISTS} 语法，确保幂等。
     * </p>
     */
    void createTableIfNotExists();

    /**
     * 返回此 Mapper 管理的表名，用于日志输出。
     * <p>
     * 由 Java 8+ 默认方法或子接口自行实现。
     * </p>
     *
     * @return 表名
     */
    String tableName();
}
