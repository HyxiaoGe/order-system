package com.hyxiao.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存消息DTO - 用于库存相关的消息传递
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMessage {
    
    private String orderId;
    private String productId;
    private Integer quantity;
    private String userId;
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
     * 处理时间
     */
    private LocalDateTime processTime;
    
    /**
     * 库存扣减结果
     */
    private boolean success;
    
    /**
     * 失败原因
     */
    private String failureReason;
}