package com.hyxiao.ordersystem.service;

import com.hyxiao.ordersystem.dto.CreateOrderRequest;
import com.hyxiao.ordersystem.dto.OrderMessage;
import com.hyxiao.ordersystem.model.Order;
import com.hyxiao.ordersystem.model.OrderStatus;
import com.hyxiao.ordersystem.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 订单服务层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final MessageProducerService messageProducerService;
    private final DelayMessageService delayMessageService;
    
    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // 生成订单ID
        String orderId = generateOrderId();
        
        // 创建订单对象
        Order order = Order.builder()
                .orderId(orderId)
                .userId(request.getUserId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .amount(request.getAmount())
                .status(OrderStatus.CREATED)
                .build();
        
        // 保存订单
        Order savedOrder = orderRepository.save(order);
        log.info("订单创建成功: {}", orderId);
        
        // 发送订单创建消息
        OrderMessage orderMessage = OrderMessage.builder()
                .orderId(savedOrder.getOrderId())
                .userId(savedOrder.getUserId())
                .productId(savedOrder.getProductId())
                .quantity(savedOrder.getQuantity())
                .amount(savedOrder.getAmount())
                .createTime(savedOrder.getCreateTime())
                .messageId(UUID.randomUUID().toString())
                .messageType("ORDER_CREATED")
                .build();
        
        messageProducerService.sendOrderCreatedMessage(orderMessage);
        
        // 发送延时取消消息（30分钟后执行）
        delayMessageService.sendOrderTimeoutCancelMessage(orderMessage, 30);
        
        return savedOrder;
    }
    
    /**
     * 根据订单ID查询订单
     */
    public Optional<Order> findOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }
    
    /**
     * 根据用户ID查询订单列表
     */
    public List<Order> findOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }
    
    /**
     * 更新订单状态
     */
    @Transactional
    public void updateOrderStatus(String orderId, OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            orderRepository.save(order);
            log.info("订单状态更新: {} -> {}", orderId, status.getDescription());
        } else {
            log.warn("订单不存在: {}", orderId);
        }
    }
    
    /**
     * 生成订单ID
     */
    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) (Math.random() * 1000));
        return "ORDER_" + timestamp + random.substring(0, Math.min(random.length(), 3));
    }
}