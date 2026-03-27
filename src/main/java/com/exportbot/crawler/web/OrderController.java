package com.exportbot.crawler.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exportbot.crawler.entity.OrderEntity;
import com.exportbot.crawler.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<IPage<OrderEntity>> listOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer orderStatus) {
        return ResponseEntity.ok(orderRepository.findPage(pageNum, pageSize, orderNo, orderStatus));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/callback")
    public ResponseEntity<?> handleCallback(@RequestBody CallbackRequest request) {
        try {
            logger.info("Received order callback: orderNo={}, status={}", request.bizOrderId, request.orderStatus);

            // 检查订单是否已存在
            OrderEntity order = orderRepository.findByOrderNo(String.valueOf(request.bizOrderId))
                    .orElse(new OrderEntity());

            order.setOrderNo(String.valueOf(request.bizOrderId));
            order.setItemId(request.itemId != null ? Long.valueOf(request.itemId) : null);
            order.setOrderStatus(request.orderStatus);
            order.setSellerId(request.sellerId != null ? Long.valueOf(request.sellerId) : null);
            order.setOriginalJson(request.toJson());

            // 新订单设置默认导出次数
            if (order.getId() == null) {
                order.setExportCount(1);
                order.setUsedCount(0);
            }

            orderRepository.save(order);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "回调处理成功",
                    "orderId", order.getId()
            ));
        } catch (Exception e) {
            logger.error("Failed to handle order callback", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * 平台回调请求体
     */
    public static class CallbackRequest {
        public Long bizOrderId;
        public Integer itemId;
        public Integer orderStatus;
        public Long sellerId;

        public String toJson() {
            return String.format(
                "{\"biz_order_id\":%d,\"item_id\":%d,\"order_status\":%d,\"seller_id\":%d}",
                bizOrderId, itemId, orderStatus, sellerId
            );
        }
    }
}
