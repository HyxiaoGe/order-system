package com.hyxiao.common.message.template;

import com.hyxiao.common.message.core.MessageSender;
import com.hyxiao.common.message.core.MessageSendResult;
import com.hyxiao.common.message.core.MessageSendCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 消息模板类 - 提供便捷的消息发送方法
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageTemplate {
    
    private final MessageSender messageSender;
    
    // ==================== 订单相关消息 ====================
    
    /**
     * 发送订单创建消息
     */
    public MessageSendResult sendOrderCreated(String orderId, Object orderMessage) {
        return messageSender.send("order-topic", "order-created", orderId, orderMessage);
    }
    
    /**
     * 发送订单超时取消消息（延时）
     */
    public MessageSendResult sendOrderTimeoutCancel(String orderId, Object orderMessage, int delayMinutes) {
        return messageSender.sendDelay("delay-cancel-topic", "order-timeout-cancel", orderId, orderMessage, delayMinutes);
    }
    
    // ==================== 库存相关消息 ====================
    
    /**
     * 发送库存扣减成功消息
     */
    public MessageSendResult sendInventoryDeductSuccess(String orderId, Object inventoryMessage) {
        return messageSender.send("inventory-topic", "inventory-success", orderId, inventoryMessage);
    }
    
    /**
     * 发送库存扣减失败消息
     */
    public MessageSendResult sendInventoryDeductFailure(String orderId, Object inventoryMessage) {
        return messageSender.send("inventory-topic", "inventory-failure", orderId, inventoryMessage);
    }
    
    // ==================== 支付相关消息 ====================
    
    /**
     * 发送支付成功消息
     */
    public MessageSendResult sendPaymentSuccess(String orderId, Object paymentMessage) {
        return messageSender.send("payment-topic", "payment-success", orderId, paymentMessage);
    }
    
    /**
     * 发送支付失败消息
     */
    public MessageSendResult sendPaymentFailure(String orderId, Object paymentMessage) {
        return messageSender.send("payment-topic", "payment-failed", orderId, paymentMessage);
    }
    
    // ==================== 通知相关消息 ====================
    
    /**
     * 发送通知消息
     */
    public MessageSendResult sendNotification(String orderId, Object notificationMessage) {
        return messageSender.send("notification-topic", "notification", orderId, notificationMessage);
    }
    
    // ==================== 通用方法 ====================
    
    /**
     * 发送自定义消息
     */
    public MessageSendResult sendCustomMessage(String topic, String tag, String key, Object message) {
        return messageSender.send(topic, tag, key, message);
    }
    
    /**
     * 异步发送消息
     */
    public void sendAsync(String topic, String tag, String key, Object message, MessageSendCallback callback) {
        messageSender.sendAsync(topic, tag, key, message, callback);
    }
    
    /**
     * 发送延时消息
     */
    public MessageSendResult sendDelayMessage(String topic, String tag, String key, Object message, int delayMinutes) {
        return messageSender.sendDelay(topic, tag, key, message, delayMinutes);
    }
}