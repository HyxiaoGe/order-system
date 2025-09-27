package com.hyxiao.ordersystem.service;

import com.hyxiao.ordersystem.dto.DelayMessage;
import com.hyxiao.ordersystem.dto.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 延时消息服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DelayMessageService {
    
    private final MessageProducerService messageProducerService;
    
    /**
     * 发送订单超时取消延时消息
     * @param orderMessage 订单消息
     * @param delayMinutes 延时分钟数
     */
    public void sendOrderTimeoutCancelMessage(OrderMessage orderMessage, int delayMinutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expectedExecuteTime = now.plusMinutes(delayMinutes);
        
        DelayMessage delayMessage = DelayMessage.builder()
                .orderId(orderMessage.getOrderId())
                .userId(orderMessage.getUserId())
                .productId(orderMessage.getProductId())
                .quantity(orderMessage.getQuantity())
                .amount(orderMessage.getAmount())
                .messageId(UUID.randomUUID().toString())
                .messageType("ORDER_TIMEOUT_CANCEL")
                .orderCreateTime(orderMessage.getCreateTime())
                .delayMinutes(delayMinutes)
                .sendTime(now)
                .expectedExecuteTime(expectedExecuteTime)
                .build();
        
        log.info("发送订单超时取消延时消息: orderId={}, delayMinutes={}, expectedExecuteTime={}", 
                orderMessage.getOrderId(), delayMinutes, expectedExecuteTime);
        
        // 发送延时消息
        messageProducerService.sendDelayMessage(delayMessage, delayMinutes);
    }
}