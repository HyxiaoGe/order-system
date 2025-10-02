package com.hyxiao.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RocketMQ配置属性
 */
@Component
@ConfigurationProperties(prefix = "rocketmq")
@Data
public class RocketMQProperties {
    
    private String endpoints;
    
    private Producer producer;
    
    private Consumer consumer;
    
    @Data
    public static class Producer {
        private String group;
    }
    
    @Data
    public static class Consumer {
        private String group;
    }
}