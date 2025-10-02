package com.hyxiao.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyxiao.common.dto.InventoryMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * 消息生产者服务 - 库存服务专用
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducerService {
    
    private final Producer rocketMQProducer;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
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