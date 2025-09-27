package com.hyxiao.ordersystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 通知记录实体类
 */
@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @Column(name = "notification_id", length = 50)
    private String notificationId;
    
    @Column(name = "order_id", length = 50, nullable = false)
    private String orderId;
    
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;
    
    @Column(name = "recipient", length = 200, nullable = false)
    private String recipient;
    
    @Column(name = "title", length = 200)
    private String title;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "template_code", length = 50)
    private String templateCode;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;
    
    @Column(name = "max_retry", nullable = false)
    private Integer maxRetry;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "external_id", length = 100)
    private String externalId;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @Column(name = "send_time")
    private LocalDateTime sendTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (retryCount == null) {
            retryCount = 0;
        }
        if (maxRetry == null) {
            maxRetry = 3;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    /**
     * 标记发送成功
     */
    public void markAsSuccess(String externalId) {
        this.status = NotificationStatus.SUCCESS;
        this.externalId = externalId;
        this.sendTime = LocalDateTime.now();
        this.failureReason = null;
    }
    
    /**
     * 标记发送失败
     */
    public void markAsFailure(String failureReason) {
        this.retryCount++;
        this.status = this.retryCount >= this.maxRetry ? 
                NotificationStatus.FAILED : NotificationStatus.PENDING;
        this.failureReason = failureReason;
    }
    
    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return retryCount < maxRetry && status == NotificationStatus.PENDING;
    }
}