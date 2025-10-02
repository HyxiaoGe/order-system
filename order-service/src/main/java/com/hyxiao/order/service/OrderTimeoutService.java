package com.hyxiao.order.service;

import com.hyxiao.common.dto.DelayMessage;
import com.hyxiao.order.model.Order;
import com.hyxiao.common.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 订单超时处理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTimeoutService {
    
    private final OrderService orderService;
    
    /**
     * 处理订单超时
     */
    @Transactional
    public void processOrderTimeout(DelayMessage delayMessage) {
        String orderId = delayMessage.getOrderId();
        log.info("开始处理订单超时: orderId={}, 创建时间={}, 预期执行时间={}", 
                orderId, delayMessage.getOrderCreateTime(), delayMessage.getExpectedExecuteTime());
        
        try {
            // 查询当前订单状态
            Optional<Order> orderOpt = orderService.findOrderById(orderId);
            if (orderOpt.isEmpty()) {
                log.warn("订单不存在，跳过超时处理: orderId={}", orderId);
                return;
            }
            
            Order order = orderOpt.get();
            OrderStatus currentStatus = order.getStatus();
            
            // 检查订单是否需要取消
            if (shouldCancelOrder(currentStatus)) {
                cancelTimeoutOrder(order, delayMessage);
            } else {
                log.info("订单状态无需取消: orderId={}, currentStatus={}", orderId, currentStatus.getDescription());
            }
            
        } catch (Exception e) {
            log.error("处理订单超时异常: orderId={}", orderId, e);
            throw e;
        }
    }
    
    /**
     * 判断订单是否应该被取消
     */
    private boolean shouldCancelOrder(OrderStatus status) {
        // 只有在 CREATED 或 INVENTORY_DEDUCTED 状态下才需要超时取消
        // 已支付(PAID)、已完成(COMPLETED)、已取消(CANCELLED)的订单不需要处理
        return status == OrderStatus.CREATED || status == OrderStatus.INVENTORY_DEDUCTED;
    }
    
    /**
     * 取消超时订单
     */
    private void cancelTimeoutOrder(Order order, DelayMessage delayMessage) {
        String orderId = order.getOrderId();
        log.info("执行订单超时取消: orderId={}, currentStatus={}", orderId, order.getStatus().getDescription());
        
        try {
            // 如果订单状态是INVENTORY_DEDUCTED，需要通知库存服务回滚
            if (order.getStatus() == OrderStatus.INVENTORY_DEDUCTED) {
                log.info("订单已扣减库存，需要回滚: orderId={}, productId={}, quantity={}", 
                        orderId, delayMessage.getProductId(), delayMessage.getQuantity());
                // TODO: 调用库存服务的释放库存接口或发送回滚消息
            }
            
            // 更新订单状态为已取消
            orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
            
            log.info("订单超时取消成功: orderId={}, 原状态={}, 取消时间={}", 
                    orderId, order.getStatus().getDescription(), LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("取消超时订单失败: orderId={}", orderId, e);
            throw e;
        }
    }
}