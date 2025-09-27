package com.hyxiao.ordersystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyxiao.ordersystem.dto.InventoryMessage;
import com.hyxiao.ordersystem.dto.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * 消息生产者服务
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
            
            // 使用 ClientServiceProvider 创建消息
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
     * 发送库存相关消息
     */
    public void sendInventoryMessage(InventoryMessage inventoryMessage, String tag) {
        try {
            String messageBody = objectMapper.writeValueAsString(inventoryMessage);
            
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            Message message = provider.newMessageBuilder()
                    .setTopic("inventory-topic")
                    .setTag(tag)
                    .setKeys(inventoryMessage.getOrderId())
                    .setBody(messageBody.getBytes(StandardCharsets.UTF_8))
                    .build();
            
            SendReceipt sendReceipt = rocketMQProducer.send(message);
            log.info("库存消息发送成功: orderId={}, tag={}, messageId={}", 
                    inventoryMessage.getOrderId(), tag, sendReceipt.getMessageId());
            
        } catch (JsonProcessingException e) {
            log.error("库存消息序列化失败: orderId={}", inventoryMessage.getOrderId(), e);
            throw new RuntimeException("消息序列化失败", e);
        } catch (Exception e) {
            log.error("库存消息发送失败: orderId={}", inventoryMessage.getOrderId(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
}