package com.hyxiao.ordersystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单消息DTO - 用于RocketMQ消息传递
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMessage {
    
    private String orderId;
    private String userId;
    private String productId;
    private Integer quantity;
    private BigDecimal amount;
    private LocalDateTime createTime;
    
    /**
     * 消息ID，用于幂等性控制
     */
    private String messageId;
    
    /**
     * 消息类型
     */
    private String messageType;
}