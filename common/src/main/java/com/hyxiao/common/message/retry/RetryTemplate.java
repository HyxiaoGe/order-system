package com.hyxiao.common.message.retry;

import com.hyxiao.common.message.config.MessageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 消息重试模板
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RetryTemplate {
    
    private final MessageProperties messageProperties;
    
    /**
     * 执行带重试的操作
     */
    public <T> T execute(String operation, Supplier<T> supplier) {
        return execute(operation, supplier, messageProperties.getRetry().getMaxAttempts());
    }
    
    /**
     * 执行带重试的操作（指定重试次数）
     */
    public <T> T execute(String operation, Supplier<T> supplier, int maxAttempts) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("执行操作: {}, 尝试次数: {}/{}", operation, attempt, maxAttempts);
                return supplier.get();
                
            } catch (Exception e) {
                lastException = e;
                log.warn("操作失败: {}, 尝试次数: {}/{}, 错误: {}", 
                        operation, attempt, maxAttempts, e.getMessage());
                
                if (attempt < maxAttempts) {
                    try {
                        long delay = calculateDelay(attempt);
                        log.debug("等待 {} ms 后重试", delay);
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }
        
        log.error("操作最终失败: {}, 已重试 {} 次", operation, maxAttempts);
        throw new RuntimeException("操作失败，已达到最大重试次数: " + maxAttempts, lastException);
    }
    
    /**
     * 计算重试延迟时间
     */
    private long calculateDelay(int attempt) {
        long baseDelay = messageProperties.getRetry().getInterval().toMillis();
        double multiplier = messageProperties.getRetry().getMultiplier();
        
        return (long) (baseDelay * Math.pow(multiplier, attempt - 1));
    }
}