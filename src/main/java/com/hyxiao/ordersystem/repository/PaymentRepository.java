package com.hyxiao.ordersystem.repository;

import com.hyxiao.ordersystem.model.Payment;
import com.hyxiao.ordersystem.model.PaymentStatus;
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
     * 根据支付状态查询支付记录
     */
    List<Payment> findByStatus(PaymentStatus status);
    
    /**
     * 查询指定时间之前创建的处理中状态的支付记录（用于超时处理）
     */
    List<Payment> findByCreateTimeBeforeAndStatus(LocalDateTime createTime, PaymentStatus status);
    
    /**
     * 根据第三方支付ID查询支付记录
     */
    Optional<Payment> findByThirdPartyPaymentId(String thirdPartyPaymentId);
}