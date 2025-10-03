package com.hyxiao.common.message.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息配置属性
 */
@Data
@ConfigurationProperties(prefix = "hyxiao.message")
public class MessageProperties {
    
    /**
     * 是否启用消息功能
     */
    private boolean enabled = true;
    
    /**
     * RocketMQ配置
     */
    private RocketMQ rocketmq = new RocketMQ();
    
    /**
     * 生产者配置
     */
    private Producer producer = new Producer();
    
    /**
     * 消费者配置
     */
    private Consumer consumer = new Consumer();
    
    /**
     * 重试配置
     */
    private Retry retry = new Retry();
    
    @Data
    public static class RocketMQ {
        /**
         * RocketMQ服务器地址
         */
        private String endpoints = "192.168.1.4:8081";
        
        /**
         * 请求超时时间
         */
        private Duration requestTimeout = Duration.ofSeconds(30);
        
        /**
         * Topic配置
         */
        private Map<String, String> topics = new HashMap<>();
    }
    
    @Data
    public static class Producer {
        /**
         * 生产者组名
         */
        private String group = "default-producer-group";
        
        /**
         * 发送超时时间
         */
        private Duration sendTimeout = Duration.ofSeconds(10);
        
        /**
         * 最大重试次数
         */
        private int maxRetries = 3;
    }
    
    @Data
    public static class Consumer {
        /**
         * 消费者组名
         */
        private String group = "default-consumer-group";
        
        /**
         * 消费超时时间
         */
        private Duration consumeTimeout = Duration.ofSeconds(30);
        
        /**
         * 最大重试次数
         */
        private int maxRetries = 3;
    }
    
    @Data
    public static class Retry {
        /**
         * 是否启用重试
         */
        private boolean enabled = true;
        
        /**
         * 最大重试次数
         */
        private int maxAttempts = 3;
        
        /**
         * 重试间隔
         */
        private Duration interval = Duration.ofSeconds(5);
        
        /**
         * 重试间隔递增倍数
         */
        private double multiplier = 2.0;
    }
}