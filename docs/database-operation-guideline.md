# 数据库操作说明书（面向 AI 模型）

> 本文档面向 AI 编程助手，说明在 auto-diagram-crawler-java 项目中如何新建数据库表、编写 CRUD 代码。
> 遵循本文档规范，模型可以完成从建表到接口全链路的代码生成。

---

## 1. 架构概览

```
SchemaAware (接口)           ← 所有需要自动建表的 Mapper 必须实现
    ├── createTableIfNotExists()   ← DDL 建表方法
    └── tableName()                ← 返回表名（用于日志）

DatabaseInitializer (组件)   ← Spring 启动时自动发现所有 SchemaAware Bean 并调用建表
```

**核心机制**：应用启动时，`DatabaseInitializer` 通过 Spring 依赖注入自动收集所有实现了 `SchemaAware` 接口的 Mapper Bean，依次调用 `createTableIfNotExists()` 完成建表。使用 `CREATE TABLE IF NOT EXISTS` 语法，幂等安全。

---

## 2. 新建一张表的完整步骤

以新建一张 `export_logs`（导出日志表）为例，需要创建/修改以下 **4 个文件**：

### 步骤 1：创建 Entity 类

**位置**: `src/main/java/com/exportbot/crawler/entity/ExportLogEntity.java`

```java
package com.exportbot.crawler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("export_logs")
public class ExportLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    // === 业务字段 ===
    private String workflowCode;
    private String status;
    private String resultMessage;
    private Long duration;

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
```

**规则**：
- 类名 = 表名的 PascalCase + `Entity` 后缀
- 必须使用 `@Data`（Lombok）、`@TableName`、`@TableId(type = IdType.AUTO)`
- **必须包含 6 个标准字段**：`id`, `createTime`, `modifyTime`, `creator`, `modifier`, `deleted`
- `@TableLogic` 标注在 `deleted` 字段上，MyBatis Plus 自动处理逻辑删除

### 步骤 2：创建 Mapper 接口

**位置**: `src/main/java/com/exportbot/crawler/mapper/ExportLogMapper.java`

```java
package com.exportbot.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exportbot.crawler.entity.ExportLogEntity;
import com.exportbot.crawler.schema.SchemaAware;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExportLogMapper extends BaseMapper<ExportLogEntity>, SchemaAware {

    // ---- SchemaAware 实现 ----

    @Override
    void createTableIfNotExists();

    @Override
    default String tableName() {
        return "export_logs";
    }

    // ---- 自定义查询方法（按需添加） ----
    // List<ExportLogEntity> selectByWorkflowCode(@Param("code") String code);
}
```

**规则**：
- **必须继承** `BaseMapper<XxxEntity>` 和 `SchemaAware`
- **必须实现** `createTableIfNotExists()` 和 `tableName()`
- `tableName()` 使用 `default` 方法直接返回表名字符串
- 简单的 CRUD 由 MyBatis Plus BaseMapper 自动提供，无需手写
- 复杂查询在此声明方法，SQL 写在 XML 中

### 步骤 3：创建 Mapper XML

**位置**: `src/main/resources/mapper/ExportLogMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.exportbot.crawler.mapper.ExportLogMapper">

    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="com.exportbot.crawler.entity.ExportLogEntity">
        <id column="id" property="id"/>
        <result column="workflow_code" property="workflowCode"/>
        <result column="status" property="status"/>
        <result column="result_message" property="resultMessage"/>
        <result column="duration" property="duration"/>
        <result column="create_time" property="createTime"/>
        <result column="modify_time" property="modifyTime"/>
        <result column="creator" property="creator"/>
        <result column="modifier" property="modifier"/>
        <result column="deleted" property="deleted"/>
    </resultMap>

    <!-- 基础列 -->
    <sql id="Base_Column_List">
        id, workflow_code, status, result_message, duration,
        create_time, modify_time, creator, modifier, deleted
    </sql>

    <!-- 建表语句，对应 SchemaAware#createTableIfNotExists() -->
    <update id="createTableIfNotExists">
        CREATE TABLE IF NOT EXISTS export_logs (
            id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',

            -- 业务字段
            workflow_code VARCHAR(64) NOT NULL COMMENT '工作流编码',
            status VARCHAR(32) NOT NULL COMMENT '状态：SUCCESS-成功，FAILED-失败',
            result_message TEXT COMMENT '结果信息',
            duration BIGINT DEFAULT 0 COMMENT '执行耗时（毫秒）',

            -- 标准字段
            create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
            modify_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
            creator VARCHAR(64) DEFAULT NULL COMMENT '创建人',
            modifier VARCHAR(64) DEFAULT NULL COMMENT '修改人',
            deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',

            -- 索引
            INDEX idx_workflow_code (workflow_code) COMMENT '工作流编码索引',
            INDEX idx_status (status) COMMENT '状态索引',
            INDEX idx_create_time (create_time) COMMENT '创建时间索引',
            INDEX idx_modify_time (modify_time) COMMENT '修改时间索引'
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导出日志表'
    </update>

    <!-- 自定义查询 SQL（按需添加） -->

</mapper>
```

**规则**：
- `namespace` 必须指向对应的 Mapper 接口全限定名
- **必须包含** `createTableIfNotExists` 的 `<update>` 块
- 建表 SQL 必须使用 `CREATE TABLE IF NOT EXISTS`
- 所有字段和表必须有 `COMMENT`
- 必须指定 `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`
- 标准字段的 SQL 模板参见下方「建表 SQL 模板」

### 步骤 4（可选）：创建 Repository 类

**位置**: `src/main/java/com/exportbot/crawler/repository/ExportLogRepository.java`

仅当需要封装复杂业务逻辑时创建。简单 CRUD 可直接注入 Mapper 使用。

```java
package com.exportbot.crawler.repository;

import com.exportbot.crawler.entity.ExportLogEntity;
import com.exportbot.crawler.mapper.ExportLogMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ExportLogRepository {

    private final ExportLogMapper exportLogMapper;

    public ExportLogRepository(ExportLogMapper exportLogMapper) {
        this.exportLogMapper = exportLogMapper;
    }

    // 封装业务方法...
}
```

---

## 3. 建表 SQL 模板

每张表的建表 SQL 必须遵循以下模板：

```sql
CREATE TABLE IF NOT EXISTS 表名 (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',

    -- 业务字段（根据需求添加）
    xxx VARCHAR(64) NOT NULL COMMENT '字段说明',

    -- 标准字段（以下 5 个字段每张表必须包含，原样复制）
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    creator VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    modifier VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',

    -- 索引（按需添加，至少包含时间索引）
    INDEX idx_create_time (create_time) COMMENT '创建时间索引',
    INDEX idx_modify_time (modify_time) COMMENT '修改时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表注释'
```

---

## 4. 字段类型速查

| Java 类型 | MySQL 类型 | 使用场景 |
|-----------|-----------|---------|
| Long | BIGINT | 主键、外键、大整数 |
| Integer | INT / TINYINT | 状态、类型、计数、布尔 |
| String | VARCHAR(N) | 编码(64)、名称(255)、长文本(500) |
| String | TEXT | 超长文本（内容、描述、JSON字符串） |
| LocalDateTime | TIMESTAMP | 时间字段 |
| BigDecimal | DECIMAL(19,4) | 金额 |

---

## 5. 命名规范速查

| 项目 | 规范 | 示例 |
|------|------|------|
| 表名 | 小写下划线，复数 | `export_logs`, `workflows` |
| Entity 类名 | PascalCase + Entity | `ExportLogEntity` |
| Mapper 接口 | PascalCase + Mapper | `ExportLogMapper` |
| Mapper XML | 与 Mapper 接口同名 | `ExportLogMapper.xml` |
| Repository | PascalCase + Repository | `ExportLogRepository` |
| 数据库列名 | 小写下划线 | `workflow_code`, `create_time` |
| Java 属性名 | camelCase | `workflowCode`, `createTime` |
| 索引名 | idx_字段名 | `idx_create_time` |
| 唯一索引名 | uk_字段名 | `uk_code` |

---

## 6. 关键文件路径

```
src/main/java/com/exportbot/crawler/
├── entity/              ← Entity 实体类
├── mapper/              ← Mapper 接口
├── repository/          ← Repository 仓库类（可选）
├── schema/
│   ├── SchemaAware.java         ← 建表接口（不要修改）
│   └── DatabaseInitializer.java ← 启动建表组件（不要修改）
└── web/                 ← Controller 层

src/main/resources/
├── mapper/              ← Mapper XML 文件
└── application.yml      ← 数据库连接配置
```

---

## 7. 检查清单

新建一张表后，用以下清单自检：

- [ ] Entity 类包含 `@TableName`、`@TableId(type = IdType.AUTO)`、`@TableLogic`
- [ ] Entity 类包含 6 个标准字段（id, createTime, modifyTime, creator, modifier, deleted）
- [ ] Mapper 接口继承 `BaseMapper<XxxEntity>` 和 `SchemaAware`
- [ ] Mapper 接口实现了 `createTableIfNotExists()` 和 `tableName()`
- [ ] Mapper XML 的 `namespace` 指向正确的 Mapper 接口
- [ ] Mapper XML 包含 `<update id="createTableIfNotExists">` 建表语句
- [ ] 建表 SQL 使用 `CREATE TABLE IF NOT EXISTS`
- [ ] 建表 SQL 包含 5 个标准字段 + 时间索引
- [ ] 所有字段有 `COMMENT`，表有 `COMMENT`
- [ ] 指定 `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`
- [ ] 项目编译通过（`mvn compile`）

---

## 8. 注意事项

1. **不需要手动注册**：只要 Mapper 实现了 `SchemaAware`，`DatabaseInitializer` 会自动发现并执行建表
2. **幂等安全**：`CREATE TABLE IF NOT EXISTS` 确保重复执行不会出错
3. **不要修改** `SchemaAware.java` 和 `DatabaseInitializer.java`
4. **逻辑删除**：所有表必须使用逻辑删除（deleted 字段），禁止物理删除
5. **字段命名映射**：MyBatis Plus 配置了 `map-underscore-to-camel-case: true`，数据库下划线自动映射到 Java 驼峰
6. **XML 文件位置**：Mapper XML 必须放在 `src/main/resources/mapper/` 目录下
7. **表结构变更**：如需给已有表加字段，直接手写 ALTER TABLE SQL 执行，不要通过代码自动化（已有决策：禁用启动时动态检查变更）
