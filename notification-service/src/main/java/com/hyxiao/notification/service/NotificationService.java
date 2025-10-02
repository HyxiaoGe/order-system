package com.hyxiao.notification.service;

import com.hyxiao.common.dto.NotificationMessage;
import com.hyxiao.common.dto.PaymentMessage;
import com.hyxiao.notification.model.Notification;
import com.hyxiao.common.enums.NotificationStatus;
import com.hyxiao.common.enums.NotificationType;
import com.hyxiao.notification.repository.NotificationRepository;
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
 * 通知服务层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final Random random = new Random();
    
    /**
     * 处理支付成功消息，发送通知
     */
    @Transactional
    public void processPaymentSuccess(PaymentMessage paymentMessage) {
        log.info("开始处理支付成功通知: orderId={}, paymentId={}", 
                paymentMessage.getOrderId(), paymentMessage.getPaymentId());
        
        try {
            // 创建并发送短信通知
            createAndSendNotification(paymentMessage, NotificationType.SMS);
            
            // 创建并发送邮件通知
            createAndSendNotification(paymentMessage, NotificationType.EMAIL);
            
            // 创建并发送App推送通知
            createAndSendNotification(paymentMessage, NotificationType.PUSH);
            
            log.info("支付成功通知处理完成: orderId={}", paymentMessage.getOrderId());
            
        } catch (Exception e) {
            log.error("处理支付成功通知异常: orderId={}", paymentMessage.getOrderId(), e);
        }
    }
    
    /**
     * 处理支付结果消息（通用入口）
     */
    @Transactional
    public void processPaymentResult(PaymentMessage paymentMessage) {
        log.info("开始处理支付结果消息: orderId={}, messageType={}, success={}", 
                paymentMessage.getOrderId(), paymentMessage.getMessageType(), paymentMessage.isSuccess());
        
        try {
            if (paymentMessage.isSuccess()) {
                // 支付成功，发送成功通知
                processPaymentSuccess(paymentMessage);
            } else {
                // 支付失败，发送失败通知
                processPaymentFailure(paymentMessage);
            }
        } catch (Exception e) {
            log.error("处理支付结果消息异常: orderId={}", paymentMessage.getOrderId(), e);
        }
    }
    
    /**
     * 处理支付失败消息，发送通知
     */
    @Transactional
    public void processPaymentFailure(PaymentMessage paymentMessage) {
        log.info("开始处理支付失败通知: orderId={}, failureReason={}", 
                paymentMessage.getOrderId(), paymentMessage.getFailureReason());
        
        try {
            // 创建并发送支付失败通知
            createAndSendFailureNotification(paymentMessage, NotificationType.SMS);
            createAndSendFailureNotification(paymentMessage, NotificationType.EMAIL);
            createAndSendFailureNotification(paymentMessage, NotificationType.PUSH);
            
            log.info("支付失败通知处理完成: orderId={}", paymentMessage.getOrderId());
            
        } catch (Exception e) {
            log.error("处理支付失败通知异常: orderId={}", paymentMessage.getOrderId(), e);
        }
    }
    
    /**
     * 创建并发送指定类型的通知
     */
    private void createAndSendNotification(PaymentMessage paymentMessage, NotificationType type) {
        try {
            // 创建通知记录
            Notification notification = createNotificationRecord(paymentMessage, type);
            
            // 模拟发送通知
            boolean sendSuccess = simulateNotificationSend(notification);
            
            if (sendSuccess) {
                // 发送成功
                handleNotificationSuccess(notification);
            } else {
                // 发送失败
                handleNotificationFailure(notification, "模拟发送失败");
            }
            
        } catch (Exception e) {
            log.error("创建并发送通知异常: orderId={}, type={}", 
                    paymentMessage.getOrderId(), type.name(), e);
        }
    }
    
    /**
     * 创建并发送支付失败通知
     */
    private void createAndSendFailureNotification(PaymentMessage paymentMessage, NotificationType type) {
        try {
            // 创建失败通知记录
            Notification notification = createFailureNotificationRecord(paymentMessage, type);
            
            // 模拟发送通知
            boolean sendSuccess = simulateNotificationSend(notification);
            
            if (sendSuccess) {
                // 发送成功
                handleNotificationSuccess(notification);
            } else {
                // 发送失败
                handleNotificationFailure(notification, "模拟发送失败");
            }
            
        } catch (Exception e) {
            log.error("创建并发送失败通知异常: orderId={}, type={}", 
                    paymentMessage.getOrderId(), type.name(), e);
        }
    }
    
    /**
     * 创建通知记录
     */
    private Notification createNotificationRecord(PaymentMessage paymentMessage, NotificationType type) {
        String notificationId = generateNotificationId();
        String recipient = generateRecipient(paymentMessage.getUserId(), type);
        
        Notification notification = Notification.builder()
                .notificationId(notificationId)
                .orderId(paymentMessage.getOrderId())
                .userId(paymentMessage.getUserId())
                .type(type)
                .status(NotificationStatus.SENDING)
                .recipient(recipient)
                .title(generateNotificationTitle(type))
                .content(generateNotificationContent(paymentMessage, type))
                .templateCode(generateTemplateCode(type))
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("通知记录创建成功: notificationId={}, type={}, orderId={}", 
                notificationId, type.name(), paymentMessage.getOrderId());
        
        return savedNotification;
    }
    
    /**
     * 创建失败通知记录
     */
    private Notification createFailureNotificationRecord(PaymentMessage paymentMessage, NotificationType type) {
        String notificationId = generateNotificationId();
        String recipient = generateRecipient(paymentMessage.getUserId(), type);
        
        Notification notification = Notification.builder()
                .notificationId(notificationId)
                .orderId(paymentMessage.getOrderId())
                .userId(paymentMessage.getUserId())
                .type(type)
                .status(NotificationStatus.SENDING)
                .recipient(recipient)
                .title(generateFailureNotificationTitle(type))
                .content(generateFailureNotificationContent(paymentMessage, type))
                .templateCode(generateFailureTemplateCode(type))
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("失败通知记录创建成功: notificationId={}, type={}, orderId={}", 
                notificationId, type.name(), paymentMessage.getOrderId());
        
        return savedNotification;
    }
    
    /**
     * 模拟通知发送（95%成功率）
     */
    private boolean simulateNotificationSend(Notification notification) {
        try {
            // 模拟发送时间（0.5-2秒）
            Thread.sleep(500 + random.nextInt(1500));
            
            // 95%的成功率
            int result = random.nextInt(100);
            boolean success = result >= 5;
            
            log.info("通知发送模拟结果: type={}, success={}, randomValue={}", 
                    notification.getType().name(), success, result);
            return success;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("通知发送被中断");
            return false;
        }
    }
    
    /**
     * 处理通知发送成功
     */
    private void handleNotificationSuccess(Notification notification) {
        // 生成外部系统ID
        String externalId = generateExternalId(notification.getType());
        
        // 更新通知记录
        notification.markAsSuccess(externalId);
        notificationRepository.save(notification);
        
        log.info("通知发送成功: notificationId={}, type={}, externalId={}", 
                notification.getNotificationId(), notification.getType().name(), externalId);
    }
    
    /**
     * 处理通知发送失败
     */
    private void handleNotificationFailure(Notification notification, String failureReason) {
        // 更新通知记录
        notification.markAsFailure(failureReason);
        notificationRepository.save(notification);
        
        log.warn("通知发送失败: notificationId={}, type={}, reason={}, retryCount={}", 
                notification.getNotificationId(), notification.getType().name(), 
                failureReason, notification.getRetryCount());
    }
    
    /**
     * 根据通知ID查询通知记录
     */
    public Optional<Notification> findNotificationById(String notificationId) {
        return notificationRepository.findById(notificationId);
    }
    
    /**
     * 根据订单ID查询通知记录
     */
    public List<Notification> findNotificationsByOrderId(String orderId) {
        return notificationRepository.findByOrderId(orderId);
    }
    
    /**
     * 根据用户ID查询通知记录
     */
    public List<Notification> findNotificationsByUserId(String userId) {
        return notificationRepository.findByUserId(userId);
    }
    
    // 辅助方法
    private String generateNotificationId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomNum = String.valueOf(random.nextInt(10000));
        return "NOTIF_" + timestamp + randomNum.substring(0, Math.min(randomNum.length(), 4));
    }
    
    private String generateRecipient(String userId, NotificationType type) {
        return switch (type) {
            case SMS -> "138****" + userId.substring(Math.max(0, userId.length() - 4));
            case EMAIL -> userId.toLowerCase() + "@example.com";
            case PUSH -> "DEVICE_" + userId.toUpperCase();
            default -> userId;
        };
    }
    
    private String generateNotificationTitle(NotificationType type) {
        return switch (type) {
            case SMS -> "支付成功通知";
            case EMAIL -> "订单支付成功确认";
            case PUSH -> "您的订单已支付成功";
            default -> "支付通知";
        };
    }
    
    private String generateNotificationContent(PaymentMessage paymentMessage, NotificationType type) {
        String baseContent = String.format("您的订单 %s 已成功支付 %.2f 元", 
                paymentMessage.getOrderId(), paymentMessage.getAmount());
        
        return switch (type) {
            case SMS -> baseContent + "，感谢您的购买！";
            case EMAIL -> "尊敬的用户，" + baseContent + "，我们将尽快为您处理订单。";
            case PUSH -> baseContent + "，点击查看订单详情。";
            default -> baseContent;
        };
    }
    
    private String generateTemplateCode(NotificationType type) {
        return switch (type) {
            case SMS -> "SMS_PAYMENT_SUCCESS_001";
            case EMAIL -> "EMAIL_PAYMENT_SUCCESS_001";
            case PUSH -> "PUSH_PAYMENT_SUCCESS_001";
            default -> "TEMPLATE_DEFAULT";
        };
    }
    
    private String generateExternalId(NotificationType type) {
        String prefix = type.name().substring(0, Math.min(3, type.name().length()));
        return prefix + "_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
    }
    
    private String generateFailureNotificationTitle(NotificationType type) {
        return switch (type) {
            case SMS -> "支付失败通知";
            case EMAIL -> "订单支付失败提醒";
            case PUSH -> "您的订单支付失败";
            default -> "支付通知";
        };
    }
    
    private String generateFailureNotificationContent(PaymentMessage paymentMessage, NotificationType type) {
        String baseContent = String.format("您的订单 %s 支付失败，失败原因：%s", 
                paymentMessage.getOrderId(), paymentMessage.getFailureReason());
        
        return switch (type) {
            case SMS -> baseContent + "，请重新尝试支付。";
            case EMAIL -> "尊敬的用户，" + baseContent + "，请检查支付信息后重新支付。";
            case PUSH -> baseContent + "，点击重新支付。";
            default -> baseContent;
        };
    }
    
    private String generateFailureTemplateCode(NotificationType type) {
        return switch (type) {
            case SMS -> "SMS_PAYMENT_FAILURE_001";
            case EMAIL -> "EMAIL_PAYMENT_FAILURE_001";
            case PUSH -> "PUSH_PAYMENT_FAILURE_001";
            default -> "TEMPLATE_FAILURE_DEFAULT";
        };
    }
}