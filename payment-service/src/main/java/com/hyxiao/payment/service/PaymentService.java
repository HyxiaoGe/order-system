package com.hyxiao.payment.service;

import com.hyxiao.common.dto.InventoryMessage;
import com.hyxiao.common.dto.PaymentMessage;
import com.hyxiao.payment.model.Payment;
import com.hyxiao.common.enums.PaymentMethod;
import com.hyxiao.common.enums.PaymentStatus;
import com.hyxiao.payment.repository.PaymentRepository;
import com.hyxiao.common.message.template.MessageTemplate;
import com.hyxiao.common.message.core.MessageSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * 支付服务层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final MessageTemplate messageTemplate;
    private final Random random = new Random();
    
    /**
     * 处理库存扣减成功消息，触发支付流程
     */
    @Transactional
    public void processInventoryDeductedSuccess(InventoryMessage inventoryMessage) {
        log.info("开始处理支付流程: orderId={}, amount={}", 
                inventoryMessage.getOrderId(), inventoryMessage.getAmount());
        
        try {
            // 创建支付记录
            Payment payment = createPaymentRecord(inventoryMessage);
            
            // 模拟支付处理
            boolean paymentSuccess = simulatePaymentProcess();
            
            if (paymentSuccess) {
                // 支付成功
                handlePaymentSuccess(payment, inventoryMessage);
            } else {
                // 支付失败
                handlePaymentFailure(payment, inventoryMessage);
            }
            
        } catch (Exception e) {
            log.error("支付处理异常: orderId={}", inventoryMessage.getOrderId(), e);
            handlePaymentError(inventoryMessage, e.getMessage());
        }
    }
    
    /**
     * 创建支付记录
     */
    private Payment createPaymentRecord(InventoryMessage inventoryMessage) {
        String paymentId = generatePaymentId();
        
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .orderId(inventoryMessage.getOrderId())
                .userId(inventoryMessage.getUserId())
                .amount(inventoryMessage.getAmount())
                .status(PaymentStatus.PROCESSING)
                .paymentMethod(getRandomPaymentMethod())
                .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        log.info("支付记录创建成功: paymentId={}, orderId={}", paymentId, inventoryMessage.getOrderId());
        
        return savedPayment;
    }
    
    /**
     * 模拟支付处理（30%失败率）
     */
    private boolean simulatePaymentProcess() {
        try {
            // 模拟支付处理时间（1-3秒）
            Thread.sleep(1000 + random.nextInt(2000));
            
            // 30%的失败率
            int result = random.nextInt(100);
            boolean success = result >= 30; // 70%成功率，30%失败率
            
            log.info("支付模拟结果: success={}, randomValue={}", success, result);
            return success;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("支付处理被中断");
            return false;
        }
    }
    
    /**
     * 处理支付成功
     */
    private void handlePaymentSuccess(Payment payment, InventoryMessage inventoryMessage) {
        // 生成第三方支付ID
        String thirdPartyPaymentId = "TPP_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
        
        // 更新支付记录
        payment.markAsSuccess(thirdPartyPaymentId);
        paymentRepository.save(payment);
        
        log.info("支付成功: paymentId={}, orderId={}, thirdPartyPaymentId={}", 
                payment.getPaymentId(), payment.getOrderId(), thirdPartyPaymentId);
        
        // 发送支付成功消息
        sendPaymentSuccessMessage(payment, inventoryMessage);
    }
    
    /**
     * 处理支付失败
     */
    private void handlePaymentFailure(Payment payment, InventoryMessage inventoryMessage) {
        String[] failureReasons = {
            "余额不足",
            "银行卡信息错误", 
            "支付密码错误",
            "网络连接超时",
            "银行系统维护",
            "支付限额超出"
        };
        
        String failureReason = failureReasons[random.nextInt(failureReasons.length)];
        
        // 更新支付记录
        payment.markAsFailure(failureReason);
        paymentRepository.save(payment);
        
        log.warn("支付失败: paymentId={}, orderId={}, reason={}", 
                payment.getPaymentId(), payment.getOrderId(), failureReason);
        
        // 发送支付失败消息
        sendPaymentFailedMessage(payment, inventoryMessage);
    }
    
    /**
     * 处理支付异常
     */
    private void handlePaymentError(InventoryMessage inventoryMessage, String errorMessage) {
        log.error("支付处理异常: orderId={}, error={}", inventoryMessage.getOrderId(), errorMessage);
        
        // 发送支付失败消息
        PaymentMessage paymentMessage = PaymentMessage.builder()
                .orderId(inventoryMessage.getOrderId())
                .userId(inventoryMessage.getUserId())
                .amount(inventoryMessage.getAmount())
                .productId(inventoryMessage.getProductId())
                .quantity(inventoryMessage.getQuantity())
                .messageId(UUID.randomUUID().toString())
                .messageType("PAYMENT_FAILED")
                .processTime(LocalDateTime.now())
                .success(false)
                .failureReason("系统异常: " + errorMessage)
                .build();
        
        messageTemplate.sendPaymentFailure(paymentMessage.getOrderId(), paymentMessage);
    }
    
    /**
     * 发送支付成功消息
     */
    private void sendPaymentSuccessMessage(Payment payment, InventoryMessage inventoryMessage) {
        PaymentMessage paymentMessage = PaymentMessage.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .thirdPartyPaymentId(payment.getThirdPartyPaymentId())
                .messageId(UUID.randomUUID().toString())
                .messageType("PAYMENT_SUCCESS")
                .processTime(LocalDateTime.now())
                .success(true)
                .build();
        
        messageTemplate.sendPaymentSuccess(paymentMessage.getOrderId(), paymentMessage);
    }
    
    /**
     * 发送支付失败消息
     */
    private void sendPaymentFailedMessage(Payment payment, InventoryMessage inventoryMessage) {
        PaymentMessage paymentMessage = PaymentMessage.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .productId(inventoryMessage.getProductId())
                .quantity(inventoryMessage.getQuantity())
                .messageId(UUID.randomUUID().toString())
                .messageType("PAYMENT_FAILED")
                .processTime(LocalDateTime.now())
                .success(false)
                .failureReason(payment.getFailureReason())
                .build();
        
        messageTemplate.sendPaymentFailure(paymentMessage.getOrderId(), paymentMessage);
    }
    
    /**
     * 根据支付ID查询支付记录
     */
    public Optional<Payment> findPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId);
    }
    
    /**
     * 根据订单ID查询支付记录
     */
    public Optional<Payment> findPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
    
    /**
     * 根据用户ID查询支付记录
     */
    public List<Payment> findPaymentsByUserId(String userId) {
        return paymentRepository.findByUserId(userId);
    }
    
    /**
     * 生成支付ID
     */
    private String generatePaymentId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomNum = String.valueOf(random.nextInt(10000));
        return "PAY_" + timestamp + randomNum.substring(0, Math.min(randomNum.length(), 4));
    }
    
    /**
     * 随机选择支付方式
     */
    private PaymentMethod getRandomPaymentMethod() {
        PaymentMethod[] methods = PaymentMethod.values();
        return methods[random.nextInt(methods.length)];
    }
}