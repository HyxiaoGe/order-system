package com.hyxiao.ordersystem.repository;

import com.hyxiao.ordersystem.model.Notification;
import com.hyxiao.ordersystem.model.NotificationStatus;
import com.hyxiao.ordersystem.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知数据访问层
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    /**
     * 根据订单ID查询通知记录
     */
    List<Notification> findByOrderId(String orderId);
    
    /**
     * 根据用户ID查询通知记录
     */
    List<Notification> findByUserId(String userId);
    
    /**
     * 根据通知状态查询通知记录
     */
    List<Notification> findByStatus(NotificationStatus status);
    
    /**
     * 根据通知类型查询通知记录
     */
    List<Notification> findByType(NotificationType type);
    
    /**
     * 查询需要重试的通知（状态为PENDING且重试次数小于最大重试次数）
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < n.maxRetry")
    List<Notification> findRetryableNotifications(@Param("status") NotificationStatus status);
    
    /**
     * 查询指定时间之前创建的失败通知
     */
    List<Notification> findByCreateTimeBeforeAndStatus(LocalDateTime createTime, NotificationStatus status);
    
    /**
     * 根据订单ID和通知类型查询通知记录
     */
    List<Notification> findByOrderIdAndType(String orderId, NotificationType type);
}