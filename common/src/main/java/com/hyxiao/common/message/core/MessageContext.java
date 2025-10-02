package com.hyxiao.common.message.core;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 消息上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageContext {
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * Topic
     */
    private String topic;
    
    /**
     * Tag
     */
    private String tag;
    
    /**
     * Key
     */
    private String key;
    
    /**
     * 消息接收时间
     */
    private long receiveTime;
    
    /**
     * 重试次数
     */
    private int retryCount;
    
    /**
     * 扩展属性
     */
    private Map<String, String> properties;
}