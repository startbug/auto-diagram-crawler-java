# 数据库建表规范

## 1. 通用字段规范

所有业务表必须包含以下标准字段：

| 字段名 | 类型 | 是否可空 | 默认值 | 说明 |
|--------|------|----------|--------|------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键，自增ID |
| create_time | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| modify_time | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 修改时间（自动更新） |
| creator | VARCHAR(64) | NULL | - | 创建人（用户ID或用户名） |
| modifier | VARCHAR(64) | NULL | - | 修改人（用户ID或用户名） |
| deleted | TINYINT | NOT NULL | 0 | 逻辑删除标志：0-未删除，1-已删除 |

## 2. 建表模板

```sql
CREATE TABLE IF NOT EXISTS 表名 (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    
    -- 业务字段（根据实际需求添加）
    -- 示例：
    -- code VARCHAR(64) NOT NULL COMMENT '业务编码',
    -- name VARCHAR(255) NOT NULL COMMENT '名称',
    
    -- 标准字段
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    creator VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    modifier VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    
    -- 索引
    INDEX idx_create_time (create_time),
    INDEX idx_modify_time (modify_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表注释';
```

## 3. 字段命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 主键 | id | id |
| 创建时间 | create_time | create_time |
| 修改时间 | modify_time | modify_time |
| 创建人 | creator | creator |
| 修改人 | modifier | modifier |
| 逻辑删除 | deleted | deleted |
| 业务编码 | xxx_code | user_code, order_no |
| 状态字段 | xxx_status | order_status, pay_status |
| 类型字段 | xxx_type | user_type, log_type |
| 是否字段 | is_xxx | is_enabled, is_deleted(已废弃，使用deleted) |
| 时间字段 | xxx_time | login_time, expire_time |
| 日期字段 | xxx_date | birth_date, start_date |
| 数量字段 | xxx_count / xxx_num | retry_count, item_num |
| 金额字段 | xxx_amount / xxx_fee | order_amount, service_fee |
| 内容/描述 | xxx_content / xxx_desc / remark | email_content, remark |

## 4. 类型规范

| 数据类型 | 使用场景 | 示例 |
|----------|----------|------|
| BIGINT | 主键ID、大整数 | id, user_id |
| INT | 状态、类型、计数 | status, type, count |
| TINYINT | 布尔值、小范围状态 | deleted, is_enabled |
| VARCHAR(64) | 编码、短标识 | code, creator |
| VARCHAR(255) | 名称、标题 | name, title |
| VARCHAR(500) | 较长文本 | summary, address |
| TEXT | 长文本内容 | content, description |
| DECIMAL(19,4) | 金额、精确小数 | amount, fee |
| TIMESTAMP | 时间戳 | create_time, modify_time |
| DATE | 日期（无时分秒） | birth_date |
| JSON | JSON数据 | config, extra_data |

## 5. 索引规范

```sql
-- 主键索引：自动创建，无需手动添加
-- PRIMARY KEY (id)

-- 唯一索引：业务唯一字段
-- UNIQUE KEY uk_code (code)

-- 普通索引：查询条件字段
-- INDEX idx_status (status)
-- INDEX idx_create_time (create_time)

-- 组合索引：常用查询组合
-- INDEX idx_status_create_time (status, create_time)

-- 全文索引：文本搜索（MySQL 5.6+）
-- FULLTEXT INDEX ft_content (content)
```

### 索引命名规范
- 主键：PRIMARY（系统自动）
- 唯一索引：uk_字段名（unique key）
- 普通索引：idx_字段名（index）
- 组合索引：idx_字段1_字段2

## 6. 注释规范

- 表必须有注释说明用途
- 每个字段必须有注释
- 状态/类型字段需说明取值含义

```sql
CREATE TABLE example (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-已完成，3-已取消',
    type INT DEFAULT 0 COMMENT '类型：1-类型A，2-类型B，3-类型C'
) COMMENT='示例表';
```

## 7. 示例：完整建表语句

```sql
-- 订单表示例
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_status TINYINT DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消',
    pay_status TINYINT DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-已退款',
    order_amount DECIMAL(19,4) DEFAULT 0.0000 COMMENT '订单金额',
    remark VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
    
    -- 标准字段
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modify_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    creator VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    modifier VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    
    -- 索引
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_order_status (order_status),
    INDEX idx_create_time (create_time),
    INDEX idx_deleted (deleted),
    INDEX idx_user_status (user_id, order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

## 8. 注意事项

1. **字符集统一**：使用 `utf8mb4` 支持完整 Unicode（包括 emoji）
2. **存储引擎**：统一使用 `InnoDB` 支持事务
3. **逻辑删除**：所有表必须实现逻辑删除，禁止物理删除
4. **时间字段**：使用 `TIMESTAMP` 类型，自动维护
5. **创建/修改人**：当前允许为空，后续关联用户表后补充约束
6. **大字段**：TEXT/BLOB 类型建议单独建表或评估存储方案
7. **分表预留**：预计数据量大的表，ID 使用雪花算法预留分表空间
