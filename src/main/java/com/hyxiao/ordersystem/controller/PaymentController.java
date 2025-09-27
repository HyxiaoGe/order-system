package com.hyxiao.ordersystem.controller;

import com.hyxiao.ordersystem.dto.ApiResponse;
import com.hyxiao.ordersystem.model.Payment;
import com.hyxiao.ordersystem.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 支付管理控制器
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * 根据支付ID查询支付记录
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentById(@PathVariable String paymentId) {
        Optional<Payment> paymentOpt = paymentService.findPaymentById(paymentId);
        if (paymentOpt.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(paymentOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 根据订单ID查询支付记录
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByOrderId(@PathVariable String orderId) {
        Optional<Payment> paymentOpt = paymentService.findPaymentByOrderId(orderId);
        if (paymentOpt.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(paymentOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 根据用户ID查询支付记录列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentsByUserId(@PathVariable String userId) {
        List<Payment> payments = paymentService.findPaymentsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("支付服务运行正常"));
    }
}