package com.exportbot.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exportbot.crawler.entity.OrderEntity;
import com.exportbot.crawler.schema.SchemaAware;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity>, SchemaAware {

    /**
     * 根据订单号查询订单
     */
    OrderEntity selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 分页查询订单列表
     */
    IPage<OrderEntity> selectOrderPage(IPage<OrderEntity> page,
                                       @Param("orderNo") String orderNo,
                                       @Param("orderStatus") Integer orderStatus);

    /**
     * 增加已使用次数
     */
    int incrementUsedCount(@Param("id") Long id);

    /**
     * 创建表（如果不存在）
     */
    @Override
    void createTableIfNotExists();

    /**
     * 返回表名
     */
    @Override
    default String tableName() {
        return "orders";
    }
}
