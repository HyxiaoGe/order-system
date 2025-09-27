package com.hyxiao.ordersystem.controller;

import com.hyxiao.ordersystem.dto.ApiResponse;
import com.hyxiao.ordersystem.dto.CreateOrderRequest;
import com.hyxiao.ordersystem.model.Order;
import com.hyxiao.ordersystem.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            log.info("接收到创建订单请求: userId={}, productId={}, quantity={}, amount={}", 
                    request.getUserId(), request.getProductId(), request.getQuantity(), request.getAmount());
            
            Order order = orderService.createOrder(request);
            return ResponseEntity.ok(ApiResponse.success("订单创建成功", order));
            
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("订单创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据订单ID查询订单
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable String orderId) {
        Optional<Order> orderOpt = orderService.findOrderById(orderId);
        if (orderOpt.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(orderOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 根据用户ID查询订单列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByUserId(@PathVariable String userId) {
        List<Order> orders = orderService.findOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("订单服务运行正常"));
    }
}