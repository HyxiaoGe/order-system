package com.hyxiao.ordersystem.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyxiao.ordersystem.dto.DelayMessage;
import com.hyxiao.ordersystem.service.OrderTimeoutService;
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
 * 延时取消消息消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DelayMessageConsumer {
    
    private final OrderTimeoutService orderTimeoutService;
    private final RocketMQProperties rocketMQProperties;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    private PushConsumer pushConsumer;
    
    @PostConstruct
    public void init() throws ClientException {
        log.info("初始化延时取消消息消费者...");
        
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(rocketMQProperties.getEndpoints());
        
        ClientConfiguration configuration = builder.build();
        
        // 创建过滤表达式，消费订单超时取消消息
        FilterExpression filterExpression = new FilterExpression("order-timeout-cancel", FilterExpressionType.TAG);
        
        // 创建消费者
        pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(configuration)
                .setConsumerGroup(rocketMQProperties.getConsumer().getGroup() + "-delay")
                .setSubscriptionExpressions(Collections.singletonMap("delay-cancel-topic", filterExpression))
                .setMessageListener(new DelayMessageListener())
                .build();
        
        log.info("延时取消消息消费者初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        if (pushConsumer != null) {
            try {
                pushConsumer.close();
                log.info("延时取消消息消费者已关闭");
            } catch (Exception e) {
                log.error("关闭延时消费者异常", e);
            }
        }
    }
    
    /**
     * 延时消息监听器
     */
    private class DelayMessageListener implements MessageListener {
        
        @Override
        public ConsumeResult consume(MessageView messageView) {
            try {
                String messageBody = StandardCharsets.UTF_8.decode(messageView.getBody()).toString();
                String tag = messageView.getTag().orElse("");
                
                log.info("接收到延时取消消息: messageId={}, tag={}, body={}", 
                        messageView.getMessageId(), tag, messageBody);
                
                // 解析消息
                DelayMessage delayMessage = objectMapper.readValue(messageBody, DelayMessage.class);
                
                // 处理订单超时取消
                if ("order-timeout-cancel".equals(tag)) {
                    orderTimeoutService.processOrderTimeout(delayMessage);
                }
                
                return ConsumeResult.SUCCESS;
                
            } catch (Exception e) {
                log.error("处理延时取消消息异常: messageId={}", messageView.getMessageId(), e);
                return ConsumeResult.FAILURE;
            }
        }
    }
}