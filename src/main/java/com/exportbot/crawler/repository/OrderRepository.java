package com.exportbot.crawler.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exportbot.crawler.entity.OrderEntity;
import com.exportbot.crawler.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrderRepository {

    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);

    private final OrderMapper orderMapper;

    public OrderRepository(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    public Optional<OrderEntity> findById(Long id) {
        return Optional.ofNullable(orderMapper.selectById(id));
    }

    public Optional<OrderEntity> findByOrderNo(String orderNo) {
        return Optional.ofNullable(orderMapper.selectByOrderNo(orderNo));
    }

    public IPage<OrderEntity> findPage(int pageNum, int pageSize, String orderNo, Integer orderStatus) {
        Page<OrderEntity> page = new Page<>(pageNum, pageSize);
        return orderMapper.selectOrderPage(page, orderNo, orderStatus);
    }

    public void save(OrderEntity order) {
        if (order.getId() == null) {
            orderMapper.insert(order);
            logger.info("Order created: {}", order.getOrderNo());
        } else {
            orderMapper.updateById(order);
            logger.info("Order updated: {}", order.getOrderNo());
        }
    }

    public boolean incrementUsedCount(Long id) {
        int rows = orderMapper.incrementUsedCount(id);
        if (rows > 0) {
            logger.info("Order used count incremented: {}", id);
        }
        return rows > 0;
    }

    public boolean hasAvailableExport(Long orderId) {
        return findById(orderId)
                .map(order -> order.getUsedCount() < order.getExportCount())
                .orElse(false);
    }
}
