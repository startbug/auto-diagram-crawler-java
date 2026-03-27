package com.exportbot.crawler.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exportbot.crawler.dto.OrderCallbackRequestDTO;
import com.exportbot.crawler.dto.OrderCallbackResponseDTO;
import com.exportbot.crawler.entity.OrderEntity;
import com.exportbot.crawler.entity.common.R;
import com.exportbot.crawler.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<R<IPage<OrderEntity>>> listOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer orderStatus) {
        return ResponseEntity.ok(R.success(orderRepository.findPage(pageNum, pageSize, orderNo, orderStatus)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<R<OrderEntity>> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(entity -> ResponseEntity.ok(R.success(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/callback")
    public ResponseEntity<R<OrderCallbackResponseDTO>> handleCallback(@RequestBody OrderCallbackRequestDTO request) {
        try {
            logger.info("Received order callback: orderNo={}, status={}", request.getBizOrderId(), request.getOrderStatus());

            // 检查订单是否已存在
            OrderEntity order = orderRepository.findByOrderNo(String.valueOf(request.getBizOrderId()))
                    .orElse(new OrderEntity());

            order.setOrderNo(String.valueOf(request.getBizOrderId()));
            order.setItemId(request.getItemId() != null ? Long.valueOf(request.getItemId()) : null);
            order.setOrderStatus(request.getOrderStatus());
            order.setSellerId(request.getSellerId() != null ? Long.valueOf(request.getSellerId()) : null);

            // TODO: originalJson 字段需要从 DTO 中传入完整的 JSON
            // order.setOriginalJson(request.toJson());

            // 新订单设置默认导出次数
            if (order.getId() == null) {
                order.setExportCount(1);
                order.setUsedCount(0);
            }

            orderRepository.save(order);

            OrderCallbackResponseDTO response = new OrderCallbackResponseDTO();
            response.setOrderId(order.getId());
            
            return ResponseEntity.ok(R.success(response));
        } catch (Exception e) {
            logger.error("Failed to handle order callback", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }
}
