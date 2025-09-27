package com.hyxiao.ordersystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付消息DTO - 用于支付相关的消息传递
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMessage {
    
    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String paymentMethod;
    
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
     * 支付结果
     */
    private boolean success;
    
    /**
     * 第三方支付ID
     */
    private String thirdPartyPaymentId;
    
    /**
     * 失败原因
     */
    private String failureReason;
    
    /**
     * 原始库存扣减消息的相关信息（用于回滚）
     */
    private String productId;
    private Integer quantity;
}