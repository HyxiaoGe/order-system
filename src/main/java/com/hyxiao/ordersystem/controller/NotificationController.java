package com.hyxiao.ordersystem.controller;

import com.hyxiao.ordersystem.dto.ApiResponse;
import com.hyxiao.ordersystem.model.Notification;
import com.hyxiao.ordersystem.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 通知管理控制器
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 根据通知ID查询通知记录
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Notification>> getNotificationById(@PathVariable String notificationId) {
        Optional<Notification> notificationOpt = notificationService.findNotificationById(notificationId);
        return notificationOpt.map(notification -> ResponseEntity.ok(ApiResponse.success(notification))).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * 根据订单ID查询通知记录列表
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotificationsByOrderId(@PathVariable String orderId) {
        List<Notification> notifications = notificationService.findNotificationsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
    
    /**
     * 根据用户ID查询通知记录列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotificationsByUserId(@PathVariable String userId) {
        List<Notification> notifications = notificationService.findNotificationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("通知服务运行正常"));
    }
}