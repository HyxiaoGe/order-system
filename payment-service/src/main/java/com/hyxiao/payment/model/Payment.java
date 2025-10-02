package com.hyxiao.payment.model;

import com.hyxiao.common.enums.PaymentStatus;
import com.hyxiao.common.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 */
@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @Column(name = "payment_id", length = 50)
    private String paymentId;
    
    @Column(name = "order_id", length = 50, nullable = false)
    private String orderId;
    
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "third_party_payment_id", length = 100)
    private String thirdPartyPaymentId;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @Column(name = "payment_time")
    private LocalDateTime paymentTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    /**
     * 标记支付成功
     */
    public void markAsSuccess(String thirdPartyPaymentId) {
        this.status = PaymentStatus.SUCCESS;
        this.thirdPartyPaymentId = thirdPartyPaymentId;
        this.paymentTime = LocalDateTime.now();
        this.failureReason = null;
    }
    
    /**
     * 标记支付失败
     */
    public void markAsFailure(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.thirdPartyPaymentId = null;
        this.paymentTime = null;
    }
}