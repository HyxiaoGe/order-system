package com.hyxiao.inventory.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ配置类 - 库存服务
 */
@Configuration
@Slf4j
public class RocketMQConfig {
    
    @Value("${rocketmq.endpoints}")
    private String endpoints;
    
    @Bean
    public Producer rocketMQProducer() {
        log.info("正在初始化RocketMQ生产者, endpoints: {}", endpoints);
        
        try {
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            
            ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                    .setEndpoints(endpoints)
                    .setRequestTimeout(java.time.Duration.ofSeconds(30));
            
            ClientConfiguration configuration = builder.build();
            
            Producer producer = provider.newProducerBuilder()
                    .setClientConfiguration(configuration)
                    .setTopics("inventory-topic")
                    .build();
            
            log.info("RocketMQ生产者初始化成功, endpoints: {}", endpoints);
            return producer;
        } catch (Exception e) {
            throw new RuntimeException("无法连接到RocketMQ服务器: " + e.getMessage(), e);
        }
    }
}