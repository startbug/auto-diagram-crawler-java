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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

            // 如果是新订单，生成 UUID
            if (order.getId() == null) {
                order.setUuid(UUID.randomUUID().toString().replace("-", ""));
                order.setOrderNo(String.valueOf(request.getBizOrderId()));
                order.setItemId(request.getItemId() != null ? Long.valueOf(request.getItemId()) : null);
                order.setOrderStatus(request.getOrderStatus());
                order.setSellerId(request.getSellerId() != null ? Long.valueOf(request.getSellerId()) : null);
                order.setExportCount(1);
                order.setUsedCount(0);
            }

            orderRepository.save(order);

            OrderCallbackResponseDTO response = new OrderCallbackResponseDTO();
            response.setOrderId(order.getId());

            // TODO 用户下单成功后，创建订单，然后生成落地页地址，推送落地页地址消息给用户
            // TODO 后续对接电商平台接口推送消息

            return ResponseEntity.ok(R.success(response));
        } catch (Exception e) {
            logger.error("Failed to handle order callback", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    /**
     * 根据订单 ID 获取落地页链接（用于测试）
     */
    @GetMapping("/{id}/entry-url")
    public ResponseEntity<R<Map<String, String>>> getEntryUrl(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    Map<String, String> data = new HashMap<>();
                    data.put("uuid", order.getUuid());
                    data.put("entryUrl", "http://localhost:5174/?uuid=" + order.getUuid());
                    return ResponseEntity.ok(R.success(data));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
