package com.hyxiao.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 延时消息DTO - 用于延时取消订单
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DelayMessage {
    
    private String orderId;
    private String userId;
    private String productId;
    private Integer quantity;
    private BigDecimal amount;
    
    /**
     * 消息ID，用于幂等性控制
     */
    private String messageId;
    
    /**
     * 消息类型
     */
    private String messageType;
    
    /**
     * 订单创建时间
     */
    private LocalDateTime orderCreateTime;
    
    /**
     * 延时时间（分钟）
     */
    private Integer delayMinutes;
    
    /**
     * 消息发送时间
     */
    private LocalDateTime sendTime;
    
    /**
     * 期望执行时间
     */
    private LocalDateTime expectedExecuteTime;
}