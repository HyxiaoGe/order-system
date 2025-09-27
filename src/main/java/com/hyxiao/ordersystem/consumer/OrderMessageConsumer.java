package com.hyxiao.ordersystem.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyxiao.ordersystem.dto.OrderMessage;
import com.hyxiao.ordersystem.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.*;
import org.apache.rocketmq.client.apis.message.MessageView;
import com.hyxiao.ordersystem.config.RocketMQProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 订单消息消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMessageConsumer {
    
    private final InventoryService inventoryService;
    private final RocketMQProperties rocketMQProperties;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    private PushConsumer pushConsumer;
    
    @PostConstruct
    public void init() throws ClientException {
        log.info("初始化订单消息消费者...");
        
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(rocketMQProperties.getEndpoints());
        
        ClientConfiguration configuration = builder.build();
        
        // 创建过滤表达式，只消费订单创建消息
        FilterExpression filterExpression = new FilterExpression("order-created", FilterExpressionType.TAG);
        
        // 创建消费者
        pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(configuration)
                .setConsumerGroup(rocketMQProperties.getConsumer().getGroup())
                .setSubscriptionExpressions(Collections.singletonMap("order-topic", filterExpression))
                .setMessageListener(new OrderCreatedMessageListener())
                .build();
        
        log.info("订单消息消费者初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        if (pushConsumer != null) {
            try {
                pushConsumer.close();
                log.info("订单消息消费者已关闭");
            } catch (Exception e) {
                log.error("关闭消费者异常", e);
            }
        }
    }
    
    /**
     * 订单创建消息监听器
     */
    private class OrderCreatedMessageListener implements MessageListener {
        
        @Override
        public ConsumeResult consume(MessageView messageView) {
            try {
                String messageBody = StandardCharsets.UTF_8.decode(messageView.getBody()).toString();
                log.info("接收到订单创建消息: messageId={}, body={}",
                        messageView.getMessageId(), messageBody);
                
                // 解析消息
                OrderMessage orderMessage = objectMapper.readValue(messageBody, OrderMessage.class);
                
                // 处理库存扣减
                inventoryService.processOrderCreated(orderMessage);
                
                log.info("订单消息处理完成: orderId={}", orderMessage.getOrderId());
                return ConsumeResult.SUCCESS;
                
            } catch (Exception e) {
                log.error("处理订单消息异常: messageId={}", messageView.getMessageId(), e);
                // 返回失败，消息会重试
                return ConsumeResult.FAILURE;
            }
        }
    }
}