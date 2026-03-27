-- 为 orders 表添加 uuid 字段
ALTER TABLE orders ADD COLUMN IF NOT EXISTS uuid VARCHAR(64) COMMENT '订单 UUID（用于落地页访问）' AFTER id;
ALTER TABLE orders ADD UNIQUE INDEX uk_uuid (uuid) COMMENT 'UUID 唯一索引';

-- 为 tasks 表添加 order_uuid 字段（冗余设计，方便查询）
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS order_uuid VARCHAR(64) COMMENT '关联的订单 UUID' AFTER order_id;
ALTER TABLE tasks ADD INDEX idx_order_uuid (order_uuid) COMMENT '订单 UUID 索引';
