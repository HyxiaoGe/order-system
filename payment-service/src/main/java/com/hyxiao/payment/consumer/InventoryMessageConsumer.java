package com.hyxiao.payment.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyxiao.common.dto.InventoryMessage;
import com.hyxiao.payment.service.PaymentService;
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
 * 库存消息消费者 - 支付服务
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryMessageConsumer {
    
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    @Value("${rocketmq.endpoints}")
    private String endpoints;
    
    @Value("${rocketmq.consumer.group}")
    private String consumerGroup;
    
    private PushConsumer pushConsumer;
    
    @PostConstruct
    public void init() throws ClientException {
        log.info("初始化库存消息消费者...");
        
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(endpoints);
        
        ClientConfiguration configuration = builder.build();
        
        // 创建过滤表达式，只消费库存扣减成功消息
        FilterExpression filterExpression = new FilterExpression("inventory-success", FilterExpressionType.TAG);
        
        // 创建消费者
        pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(configuration)
                .setConsumerGroup(consumerGroup)
                .setSubscriptionExpressions(Collections.singletonMap("inventory-topic", filterExpression))
                .setMessageListener(new InventorySuccessMessageListener())
                .build();
        
        log.info("库存消息消费者初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        if (pushConsumer != null) {
            try {
                pushConsumer.close();
                log.info("库存消息消费者已关闭");
            } catch (Exception e) {
                log.error("关闭消费者异常", e);
            }
        }
    }
    
    /**
     * 库存扣减成功消息监听器
     */
    private class InventorySuccessMessageListener implements MessageListener {
        
        @Override
        public ConsumeResult consume(MessageView messageView) {
            try {
                String messageBody = StandardCharsets.UTF_8.decode(messageView.getBody()).toString();
                log.info("接收到库存扣减成功消息: messageId={}, body={}",
                        messageView.getMessageId(), messageBody);
                
                // 解析消息
                InventoryMessage inventoryMessage = objectMapper.readValue(messageBody, InventoryMessage.class);
                
                // 处理支付流程
                paymentService.processInventoryDeductedSuccess(inventoryMessage);
                
                log.info("支付流程处理完成: orderId={}", inventoryMessage.getOrderId());
                return ConsumeResult.SUCCESS;
                
            } catch (Exception e) {
                log.error("处理库存消息异常: messageId={}", messageView.getMessageId(), e);
                // 返回失败，消息会重试
                return ConsumeResult.FAILURE;
            }
        }
    }
}