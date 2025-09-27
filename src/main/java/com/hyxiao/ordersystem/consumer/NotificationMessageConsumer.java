package com.hyxiao.ordersystem.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyxiao.ordersystem.dto.NotificationMessage;
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
 * 通知消息消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationMessageConsumer {
    
    private final OrderService orderService;
    private final RocketMQProperties rocketMQProperties;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    private PushConsumer pushConsumer;
    
    @PostConstruct
    public void init() throws ClientException {
        log.info("初始化通知消息消费者...");
        
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(rocketMQProperties.getEndpoints());
        
        ClientConfiguration configuration = builder.build();
        
        // 创建过滤表达式，消费通知成功和失败消息
        FilterExpression filterExpression = new FilterExpression("notification-success || notification-failed", FilterExpressionType.TAG);
        
        // 创建消费者
        pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(configuration)
                .setConsumerGroup(rocketMQProperties.getConsumer().getGroup() + "-notification")
                .setSubscriptionExpressions(Collections.singletonMap("notification-topic", filterExpression))
                .setMessageListener(new NotificationResultMessageListener())
                .build();
        
        log.info("通知消息消费者初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        if (pushConsumer != null) {
            try {
                pushConsumer.close();
                log.info("通知消息消费者已关闭");
            } catch (Exception e) {
                log.error("关闭通知消费者异常", e);
            }
        }
    }
    
    /**
     * 通知结果消息监听器
     */
    private class NotificationResultMessageListener implements MessageListener {
        
        @Override
        public ConsumeResult consume(MessageView messageView) {
            try {
                String messageBody = StandardCharsets.UTF_8.decode(messageView.getBody()).toString();
                String tag = messageView.getTag().orElse("");
                
                log.info("接收到通知结果消息: messageId={}, tag={}, body={}", 
                        messageView.getMessageId(), tag, messageBody);
                
                // 解析消息
                NotificationMessage notificationMessage = objectMapper.readValue(messageBody, NotificationMessage.class);
                
                // 根据消息类型处理
                if ("notification-success".equals(tag)) {
                    // 通知发送成功
                    handleNotificationSuccess(notificationMessage);
                    
                } else if ("notification-failed".equals(tag)) {
                    // 通知发送失败
                    handleNotificationFailure(notificationMessage);
                }
                
                return ConsumeResult.SUCCESS;
                
            } catch (Exception e) {
                log.error("处理通知结果消息异常: messageId={}", messageView.getMessageId(), e);
                return ConsumeResult.FAILURE;
            }
        }
        
        /**
         * 处理通知发送成功
         */
        private void handleNotificationSuccess(NotificationMessage notificationMessage) {
            log.info("通知发送成功: orderId={}, type={}, externalId={}", 
                    notificationMessage.getOrderId(), 
                    notificationMessage.getNotificationType(),
                    notificationMessage.getExternalId());
            
            // 检查是否所有类型的通知都已成功发送
            // 这里可以根据业务需求决定是否更新订单状态为COMPLETED
            // 为了简化，我们暂时不做特殊处理
        }
        
        /**
         * 处理通知发送失败
         */
        private void handleNotificationFailure(NotificationMessage notificationMessage) {
            log.warn("通知发送失败: orderId={}, type={}, reason={}, retryCount={}", 
                    notificationMessage.getOrderId(),
                    notificationMessage.getNotificationType(),
                    notificationMessage.getFailureReason(),
                    notificationMessage.getRetryCount());
            
            // 这里可以实现通知失败的补偿逻辑
            // 比如记录失败原因、发送警告邮件给管理员等
            // 如果重试次数达到上限，可以考虑人工介入
            
            if (notificationMessage.getRetryCount() != null && notificationMessage.getRetryCount() >= 3) {
                log.error("通知发送彻底失败，需要人工处理: orderId={}, type={}", 
                        notificationMessage.getOrderId(), notificationMessage.getNotificationType());
                // 这里可以发送告警通知给运维人员
            }
        }
    }
}