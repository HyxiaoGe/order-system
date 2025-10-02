package com.hyxiao.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 通知消息DTO - 用于通知相关的消息传递
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage {
    
    private String notificationId;
    private String orderId;
    private String userId;
    private String paymentId;
    private BigDecimal amount;
    private String paymentMethod;
    
    /**
     * 通知类型
     */
    private String notificationType;
    
    /**
     * 接收方信息
     */
    private String recipient;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 模板代码
     */
    private String templateCode;
    
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
     * 发送结果
     */
    private boolean success;
    
    /**
     * 外部系统返回的ID
     */
    private String externalId;
    
    /**
     * 失败原因
     */
    private String failureReason;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
}