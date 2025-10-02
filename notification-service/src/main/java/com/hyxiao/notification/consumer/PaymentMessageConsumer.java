package com.hyxiao.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyxiao.common.dto.PaymentMessage;
import com.hyxiao.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.*;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 支付消息消费者 - 通知服务
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentMessageConsumer {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    @Value("${rocketmq.endpoints}")
    private String endpoints;
    
    @Value("${rocketmq.consumer.group}")
    private String consumerGroup;
    
    private PushConsumer pushConsumer;
    
    @PostConstruct
    public void init() throws ClientException {
        log.info("初始化支付消息消费者...");
        
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(endpoints);
        
        ClientConfiguration configuration = builder.build();
        
        // 创建过滤表达式，只消费支付成功消息
        FilterExpression filterExpression = new FilterExpression("payment-success", FilterExpressionType.TAG);
        
        // 创建消费者
        pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(configuration)
                .setConsumerGroup(consumerGroup)
                .setSubscriptionExpressions(Collections.singletonMap("payment-topic", filterExpression))
                .setMessageListener(new PaymentSuccessMessageListener())
                .build();
        
        log.info("支付消息消费者初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        if (pushConsumer != null) {
            try {
                pushConsumer.close();
                log.info("支付消息消费者已关闭");
            } catch (Exception e) {
                log.error("关闭消费者异常", e);
            }
        }
    }
    
    /**
     * 支付成功消息监听器
     */
    private class PaymentSuccessMessageListener implements MessageListener {
        
        @Override
        public ConsumeResult consume(MessageView messageView) {
            try {
                String messageBody = StandardCharsets.UTF_8.decode(messageView.getBody()).toString();
                log.info("接收到支付成功消息: messageId={}, body={}",
                        messageView.getMessageId(), messageBody);
                
                // 解析消息
                PaymentMessage paymentMessage = objectMapper.readValue(messageBody, PaymentMessage.class);
                
                // 处理通知发送
                notificationService.processPaymentSuccess(paymentMessage);
                
                log.info("支付成功通知处理完成: orderId={}", paymentMessage.getOrderId());
                return ConsumeResult.SUCCESS;
                
            } catch (Exception e) {
                log.error("处理支付消息异常: messageId={}", messageView.getMessageId(), e);
                // 返回失败，消息会重试
                return ConsumeResult.FAILURE;
            }
        }
    }
}