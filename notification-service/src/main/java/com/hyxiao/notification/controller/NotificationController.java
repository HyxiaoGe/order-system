package com.hyxiao.notification.controller;

import com.hyxiao.common.dto.ApiResponse;
import com.hyxiao.notification.model.Notification;
import com.hyxiao.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 通知控制器
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
        Optional<Notification> notification = notificationService.findNotificationById(notificationId);
        if (notification.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(notification.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 根据订单ID查询通知记录
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotificationsByOrderId(@PathVariable String orderId) {
        List<Notification> notifications = notificationService.findNotificationsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
    
    /**
     * 根据用户ID查询通知记录
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotificationsByUserId(@PathVariable String userId) {
        List<Notification> notifications = notificationService.findNotificationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("通知服务运行正常"));
    }
}