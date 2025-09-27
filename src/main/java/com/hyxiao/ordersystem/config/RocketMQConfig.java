package com.hyxiao.ordersystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ配置类
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RocketMQConfig {
    
    private final RocketMQProperties rocketMQProperties;
    
    @Bean
    public Producer rocketMQProducer() {
        log.info("正在初始化RocketMQ生产者, endpoints: {}", rocketMQProperties.getEndpoints());
        
        try {
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            
            ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                    .setEndpoints(rocketMQProperties.getEndpoints())
                    .setRequestTimeout(java.time.Duration.ofSeconds(30)); // 增加超时时间
            
            ClientConfiguration configuration = builder.build();
            
            Producer producer = provider.newProducerBuilder()
                    .setClientConfiguration(configuration)
                    .setTopics("order-topic", "inventory-topic", "payment-topic", "notification-topic", "delay-cancel-topic")
                    .build();
            
            log.info("RocketMQ生产者初始化成功, endpoints: {}", rocketMQProperties.getEndpoints());
            return producer;
            
        } catch (Exception e) {
            log.error("RocketMQ生产者初始化失败, endpoints: {}", rocketMQProperties.getEndpoints(), e);
            log.error("请检查:");
            log.error("1. RocketMQ服务是否正常运行");
            log.error("2. 网络连接是否正常");
            log.error("3. endpoints配置是否正确: {}", rocketMQProperties.getEndpoints());
            throw new RuntimeException("无法连接到RocketMQ服务器: " + e.getMessage(), e);
        }
    }
}