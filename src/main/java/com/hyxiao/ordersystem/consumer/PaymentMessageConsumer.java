package com.hyxiao.ordersystem.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyxiao.ordersystem.dto.PaymentMessage;
import com.hyxiao.ordersystem.model.OrderStatus;
import com.hyxiao.ordersystem.service.InventoryService;
import com.hyxiao.ordersystem.service.NotificationService;
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
 * 支付消息消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentMessageConsumer {
    
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final RocketMQProperties rocketMQProperties;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    private PushConsumer pushConsumer;
    
    @PostConstruct
    public void init() throws ClientException {
        log.info("初始化支付消息消费者...");
        
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(rocketMQProperties.getEndpoints());
        
        ClientConfiguration configuration = builder.build();
        
        // 创建过滤表达式，消费支付成功和失败消息
        FilterExpression filterExpression = new FilterExpression("payment-success || payment-failed", FilterExpressionType.TAG);
        
        // 创建消费者
        pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(configuration)
                .setConsumerGroup(rocketMQProperties.getConsumer().getGroup() + "-payment")
                .setSubscriptionExpressions(Collections.singletonMap("payment-topic", filterExpression))
                .setMessageListener(new PaymentResultMessageListener())
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
                log.error("关闭支付消费者异常", e);
            }
        }
    }
    
    /**
     * 支付结果消息监听器
     */
    private class PaymentResultMessageListener implements MessageListener {
        
        @Override
        public ConsumeResult consume(MessageView messageView) {
            try {
                String messageBody = StandardCharsets.UTF_8.decode(messageView.getBody()).toString();
                String tag = messageView.getTag().orElse("");
                
                log.info("接收到支付结果消息: messageId={}, tag={}, body={}", 
                        messageView.getMessageId(), tag, messageBody);
                
                // 解析消息
                PaymentMessage paymentMessage = objectMapper.readValue(messageBody, PaymentMessage.class);
                
                // 根据消息类型处理
                if ("payment-success".equals(tag)) {
                    // 支付成功，更新订单状态并触发通知流程
                    orderService.updateOrderStatus(paymentMessage.getOrderId(), OrderStatus.PAID);
                    log.info("订单支付成功，状态已更新: orderId={}, paymentId={}", 
                            paymentMessage.getOrderId(), paymentMessage.getPaymentId());
                    
                    // 触发通知流程
                    notificationService.processPaymentSuccess(paymentMessage);
                    
                } else if ("payment-failed".equals(tag)) {
                    // 支付失败，取消订单并回滚库存
                    handlePaymentFailure(paymentMessage);
                }
                
                return ConsumeResult.SUCCESS;
                
            } catch (Exception e) {
                log.error("处理支付结果消息异常: messageId={}", messageView.getMessageId(), e);
                return ConsumeResult.FAILURE;
            }
        }
        
        /**
         * 处理支付失败场景
         */
        private void handlePaymentFailure(PaymentMessage paymentMessage) {
            try {
                // 更新订单状态为已取消
                orderService.updateOrderStatus(paymentMessage.getOrderId(), OrderStatus.CANCELLED);
                log.info("支付失败，订单已取消: orderId={}, reason={}", 
                        paymentMessage.getOrderId(), paymentMessage.getFailureReason());
                
                // 回滚库存（如果有库存扣减信息）
                if (paymentMessage.getProductId() != null && paymentMessage.getQuantity() != null) {
                    inventoryService.releaseInventory(
                            paymentMessage.getProductId(), 
                            paymentMessage.getQuantity(), 
                            paymentMessage.getOrderId()
                    );
                    log.info("支付失败，库存已回滚: orderId={}, productId={}, quantity={}", 
                            paymentMessage.getOrderId(), paymentMessage.getProductId(), paymentMessage.getQuantity());
                }
                
            } catch (Exception e) {
                log.error("处理支付失败异常: orderId={}", paymentMessage.getOrderId(), e);
                throw e;
            }
        }
    }
}