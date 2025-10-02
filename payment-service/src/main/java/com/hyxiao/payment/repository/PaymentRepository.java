package com.hyxiao.payment.repository;

import com.hyxiao.payment.model.Payment;
import com.hyxiao.common.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 支付数据访问层
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    /**
     * 根据订单ID查询支付记录
     */
    Optional<Payment> findByOrderId(String orderId);
    
    /**
     * 根据用户ID查询支付记录
     */
    List<Payment> findByUserId(String userId);
    
    /**
     * 根据状态查询支付记录
     */
    List<Payment> findByStatus(PaymentStatus status);
    
    /**
     * 查询指定时间范围内的支付记录
     */
    List<Payment> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据第三方支付ID查询
     */
    Optional<Payment> findByThirdPartyPaymentId(String thirdPartyPaymentId);
}