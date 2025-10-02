package com.hyxiao.common.message.rocketmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyxiao.common.message.config.MessageProperties;
import com.hyxiao.common.message.core.*;
import com.hyxiao.common.message.monitor.MessageMetrics;
import com.hyxiao.common.message.retry.RetryTemplate;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * RocketMQ消息发送器实现
 */
@Component
@Slf4j
public class RocketMQMessageSender implements MessageSender {
    
    private final Producer producer;
    private final MessageProperties messageProperties;
    private final MessageMetrics messageMetrics;
    private final RetryTemplate retryTemplate;
    private final ObjectMapper objectMapper;
    
    public RocketMQMessageSender(Producer producer, 
                                MessageProperties messageProperties,
                                MessageMetrics messageMetrics,
                                RetryTemplate retryTemplate) {
        this.producer = producer;
        this.messageProperties = messageProperties;
        this.messageMetrics = messageMetrics;
        this.retryTemplate = retryTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }
    
    @Override
    public MessageSendResult send(String topic, String tag, String key, Object message) {
        Timer.Sample sample = messageMetrics.startSendTimer(topic, tag);
        
        try {
            MessageSendResult result = retryTemplate.execute(
                String.format("send message: %s/%s", topic, tag),
                () -> doSend(topic, tag, key, message)
            );
            
            messageMetrics.recordSendSuccess(topic, tag);
            return result;
            
        } catch (Exception e) {
            messageMetrics.recordSendFailure(topic, tag, e.getClass().getSimpleName());
            return MessageSendResult.failure("消息发送失败: " + e.getMessage(), e);
        } finally {
            messageMetrics.endSendTimer(sample, topic, tag);
        }
    }
    
    private MessageSendResult doSend(String topic, String tag, String key, Object message) {
        try {
            String messageBody = serializeMessage(message);
            
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            Message mqMessage = provider.newMessageBuilder()
                    .setTopic(topic)
                    .setTag(tag)
                    .setKeys(key)
                    .setBody(messageBody.getBytes(StandardCharsets.UTF_8))
                    .build();
            
            SendReceipt sendReceipt = producer.send(mqMessage);
            
            log.info("消息发送成功: topic={}, tag={}, key={}, messageId={}", 
                    topic, tag, key, sendReceipt.getMessageId());
            
            return MessageSendResult.success(sendReceipt.getMessageId().toString());
            
        } catch (Exception e) {
            log.error("消息发送失败: topic={}, tag={}, key={}", topic, tag, key, e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
    
    @Override
    public MessageSendResult sendDelay(String topic, String tag, String key, Object message, int delayMinutes) {
        try {
            String messageBody = serializeMessage(message);
            
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            Message mqMessage = provider.newMessageBuilder()
                    .setTopic(topic)
                    .setTag(tag)
                    .setKeys(key)
                    .setBody(messageBody.getBytes(StandardCharsets.UTF_8))
                    .setDeliveryTimestamp(System.currentTimeMillis() + (delayMinutes * 60 * 1000L))
                    .build();
            
            SendReceipt sendReceipt = producer.send(mqMessage);
            
            log.info("延时消息发送成功: topic={}, tag={}, key={}, delayMinutes={}, messageId={}", 
                    topic, tag, key, delayMinutes, sendReceipt.getMessageId());
            
            return MessageSendResult.success(sendReceipt.getMessageId().toString());
            
        } catch (Exception e) {
            log.error("延时消息发送失败: topic={}, tag={}, key={}, delayMinutes={}", 
                    topic, tag, key, delayMinutes, e);
            return MessageSendResult.failure("延时消息发送失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void sendAsync(String topic, String tag, String key, Object message, MessageSendCallback callback) {
        CompletableFuture.runAsync(() -> {
            MessageSendResult result = send(topic, tag, key, message);
            if (result.isSuccess()) {
                callback.onSuccess(result);
            } else {
                callback.onFailure(result);
            }
        });
    }
    
    /**
     * 序列化消息
     */
    private String serializeMessage(Object message) throws JsonProcessingException {
        if (message instanceof String) {
            return (String) message;
        }
        return objectMapper.writeValueAsString(message);
    }
}