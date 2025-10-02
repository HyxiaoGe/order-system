package com.hyxiao.common.message.core;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 消息发送结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendResult {
    
    /**
     * 是否发送成功
     */
    private boolean success;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 发送时间戳
     */
    private long timestamp;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 异常信息
     */
    private Throwable exception;
    
    /**
     * 创建成功结果
     */
    public static MessageSendResult success(String messageId) {
        return MessageSendResult.builder()
                .success(true)
                .messageId(messageId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建失败结果
     */
    public static MessageSendResult failure(String errorMessage, Throwable exception) {
        return MessageSendResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .exception(exception)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}