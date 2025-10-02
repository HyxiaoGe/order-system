package com.hyxiao.common.message.autoconfigure;

import com.hyxiao.common.message.config.MessageProperties;
import com.hyxiao.common.message.core.MessageSender;
import com.hyxiao.common.message.monitor.MessageMetrics;
import com.hyxiao.common.message.retry.RetryTemplate;
import com.hyxiao.common.message.rocketmq.RocketMQMessageSender;
import com.hyxiao.common.message.template.MessageTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消息自动配置类
 */
@Configuration
@EnableConfigurationProperties(MessageProperties.class)
@ConditionalOnProperty(prefix = "hyxiao.message", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class MessageAutoConfiguration {
    
    @Bean
    public Producer rocketMQProducer(MessageProperties messageProperties) {
        log.info("初始化RocketMQ生产者: endpoints={}", messageProperties.getRocketmq().getEndpoints());
        
        try {
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            
            ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                    .setEndpoints(messageProperties.getRocketmq().getEndpoints())
                    .setRequestTimeout(messageProperties.getRocketmq().getRequestTimeout());
            
            ClientConfiguration configuration = builder.build();
            
            Producer producer = provider.newProducerBuilder()
                    .setClientConfiguration(configuration)
                    .setTopics(
                        "order-topic", 
                        "inventory-topic", 
                        "payment-topic", 
                        "notification-topic", 
                        "delay-cancel-topic"
                    )
                    .build();
            
            log.info("RocketMQ生产者初始化成功");
            return producer;
            
        } catch (Exception e) {
            log.error("RocketMQ生产者初始化失败", e);
            throw new RuntimeException("无法连接到RocketMQ服务器: " + e.getMessage(), e);
        }
    }
    
    @Bean
    public MessageMetrics messageMetrics(MeterRegistry meterRegistry) {
        return new MessageMetrics(meterRegistry);
    }
    
    @Bean
    public RetryTemplate retryTemplate(MessageProperties messageProperties) {
        return new RetryTemplate(messageProperties);
    }
    
    @Bean
    public MessageSender messageSender(Producer producer, 
                                      MessageProperties messageProperties,
                                      MessageMetrics messageMetrics,
                                      RetryTemplate retryTemplate) {
        return new RocketMQMessageSender(producer, messageProperties, messageMetrics, retryTemplate);
    }
    
    @Bean
    public MessageTemplate messageTemplate(MessageSender messageSender) {
        return new MessageTemplate(messageSender);
    }
}