package com.hyxiao.ordersystem.model;

/**
 * 通知状态枚举
 */
public enum NotificationStatus {
    
    /**
     * 待发送
     */
    PENDING("待发送"),
    
    /**
     * 发送中
     */
    SENDING("发送中"),
    
    /**
     * 发送成功
     */
    SUCCESS("发送成功"),
    
    /**
     * 发送失败
     */
    FAILED("发送失败"),
    
    /**
     * 已取消
     */
    CANCELLED("已取消");
    
    private final String description;
    
    NotificationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}