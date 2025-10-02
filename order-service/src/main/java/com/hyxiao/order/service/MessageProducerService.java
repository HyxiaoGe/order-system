package com.hyxiao.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyxiao.common.dto.DelayMessage;
import com.hyxiao.common.dto.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * 消息生产者服务 - 订单服务专用
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducerService {
    
    private final Producer rocketMQProducer;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    /**
     * 发送订单创建消息
     */
    public void sendOrderCreatedMessage(OrderMessage orderMessage) {
        try {
            String messageBody = objectMapper.writeValueAsString(orderMessage);
            
            // 根据RocketMQ 5.0.8 API创建消息
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            Message message = provider.newMessageBuilder()
                    .setTopic("order-topic")
                    .setTag("order-created")
                    .setKeys(orderMessage.getOrderId())
                    .setBody(messageBody.getBytes(StandardCharsets.UTF_8))
                    .build();
            
            SendReceipt sendReceipt = rocketMQProducer.send(message);
            log.info("订单创建消息发送成功: orderId={}, messageId={}", 
                    orderMessage.getOrderId(), sendReceipt.getMessageId());
            
        } catch (JsonProcessingException e) {
            log.error("订单消息序列化失败: {}", orderMessage.getOrderId(), e);
            throw new RuntimeException("消息序列化失败", e);
        } catch (Exception e) {
            log.error("订单创建消息发送失败: {}", orderMessage.getOrderId(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
    
    /**
     * 发送延时消息
     */
    public void sendDelayMessage(DelayMessage delayMessage, int delayMinutes) {
        try {
            String messageBody = objectMapper.writeValueAsString(delayMessage);
            
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            Message message = provider.newMessageBuilder()
                    .setTopic("delay-cancel-topic")
                    .setTag("order-timeout-cancel")
                    .setKeys(delayMessage.getOrderId())
                    .setBody(messageBody.getBytes(StandardCharsets.UTF_8))
                    // 设置延时时间（毫秒）
                    .setDeliveryTimestamp(System.currentTimeMillis() + (delayMinutes * 60 * 1000L))
                    .build();
            
            SendReceipt sendReceipt = rocketMQProducer.send(message);
            log.info("延时消息发送成功: orderId={}, delayMinutes={}, messageId={}", 
                    delayMessage.getOrderId(), delayMinutes, sendReceipt.getMessageId());
            
        } catch (JsonProcessingException e) {
            log.error("延时消息序列化失败: orderId={}", delayMessage.getOrderId(), e);
            throw new RuntimeException("消息序列化失败", e);
        } catch (Exception e) {
            log.error("延时消息发送失败: orderId={}", delayMessage.getOrderId(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
    
    /**
     * 发送库存相关消息
     */
    public void sendInventoryMessage(Object inventoryMessage, String tag) {
        try {
            String messageBody = objectMapper.writeValueAsString(inventoryMessage);
            
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            Message message = provider.newMessageBuilder()
                    .setTopic("inventory-topic")
                    .setTag(tag)
                    .setKeys(inventoryMessage.toString()) // 简化处理
                    .setBody(messageBody.getBytes(StandardCharsets.UTF_8))
                    .build();
            
            SendReceipt sendReceipt = rocketMQProducer.send(message);
            log.info("库存消息发送成功: tag={}, messageId={}", 
                    tag, sendReceipt.getMessageId());
            
        } catch (JsonProcessingException e) {
            log.error("库存消息序列化失败", e);
            throw new RuntimeException("消息序列化失败", e);
        } catch (Exception e) {
            log.error("库存消息发送失败", e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
}