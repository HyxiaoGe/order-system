package com.hyxiao.common.message.core;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 消息处理结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageProcessResult {
    
    /**
     * 处理结果状态
     */
    private ProcessStatus status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 异常信息
     */
    private Throwable exception;
    
    /**
     * 是否需要重试
     */
    private boolean needRetry;
    
    /**
     * 创建成功结果
     */
    public static MessageProcessResult success() {
        return MessageProcessResult.builder()
                .status(ProcessStatus.SUCCESS)
                .needRetry(false)
                .build();
    }
    
    /**
     * 创建失败结果（需要重试）
     */
    public static MessageProcessResult retryableFail(String errorMessage, Throwable exception) {
        return MessageProcessResult.builder()
                .status(ProcessStatus.FAILURE)
                .errorMessage(errorMessage)
                .exception(exception)
                .needRetry(true)
                .build();
    }
    
    /**
     * 创建失败结果（不需要重试）
     */
    public static MessageProcessResult nonRetryableFail(String errorMessage, Throwable exception) {
        return MessageProcessResult.builder()
                .status(ProcessStatus.FAILURE)
                .errorMessage(errorMessage)
                .exception(exception)
                .needRetry(false)
                .build();
    }
    
    /**
     * 处理状态枚举
     */
    public enum ProcessStatus {
        /**
         * 处理成功
         */
        SUCCESS,
        
        /**
         * 处理失败
         */
        FAILURE,
        
        /**
         * 忽略消息
         */
        IGNORE
    }
}