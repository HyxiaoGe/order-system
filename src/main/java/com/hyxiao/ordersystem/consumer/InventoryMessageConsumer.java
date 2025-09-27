package com.hyxiao.ordersystem.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyxiao.ordersystem.dto.InventoryMessage;
import com.hyxiao.ordersystem.model.OrderStatus;
import com.hyxiao.ordersystem.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.message.MessageView;
import com.hyxiao.ordersystem.config.RocketMQProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 库存消息消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryMessageConsumer {
    
    private final OrderService orderService;
    private final RocketMQProperties rocketMQProperties;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    private PushConsumer pushConsumer;
    
    @PostConstruct
    public void init() throws ClientException {
        log.info("初始化库存消息消费者...");
        
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(rocketMQProperties.getEndpoints());
        
        ClientConfiguration configuration = builder.build();
        
        // 创建过滤表达式，消费库存成功和失败消息
        FilterExpression filterExpression = new FilterExpression("inventory-success || inventory-failed", FilterExpressionType.TAG);
        
        // 创建消费者
        pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(configuration)
                .setConsumerGroup(rocketMQProperties.getConsumer().getGroup() + "-inventory")
                .setSubscriptionExpressions(Collections.singletonMap("inventory-topic", filterExpression))
                .setMessageListener(new InventoryResultMessageListener())
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
                log.error("关闭库存消费者异常", e);
            }
        }
    }
    
    /**
     * 库存结果消息监听器
     */
    private class InventoryResultMessageListener implements MessageListener {
        
        @Override
        public ConsumeResult consume(MessageView messageView) {
            try {
                String messageBody = StandardCharsets.UTF_8.decode(messageView.getBody()).toString();
                String tag = messageView.getTag().orElse("");
                
                log.info("接收到库存结果消息: messageId={}, tag={}, body={}", 
                        messageView.getMessageId(), tag, messageBody);
                
                // 解析消息
                InventoryMessage inventoryMessage = objectMapper.readValue(messageBody, InventoryMessage.class);
                
                // 根据消息类型处理
                if ("inventory-success".equals(tag)) {
                    // 库存扣减成功，更新订单状态
                    orderService.updateOrderStatus(inventoryMessage.getOrderId(), OrderStatus.INVENTORY_DEDUCTED);
                    log.info("订单库存扣减成功，状态已更新: orderId={}", inventoryMessage.getOrderId());
                    
                } else if ("inventory-failed".equals(tag)) {
                    // 库存扣减失败，取消订单
                    orderService.updateOrderStatus(inventoryMessage.getOrderId(), OrderStatus.CANCELLED);
                    log.info("订单库存扣减失败，订单已取消: orderId={}, reason={}", 
                            inventoryMessage.getOrderId(), inventoryMessage.getFailureReason());
                }
                
                return ConsumeResult.SUCCESS;
                
            } catch (Exception e) {
                log.error("处理库存结果消息异常: messageId={}", messageView.getMessageId(), e);
                return ConsumeResult.FAILURE;
            }
        }
    }
}