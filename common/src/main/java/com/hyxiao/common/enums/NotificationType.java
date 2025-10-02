package com.hyxiao.common.enums;

/**
 * 通知类型枚举
 */
public enum NotificationType {
    
    /**
     * 短信通知
     */
    SMS("短信通知"),
    
    /**
     * 邮件通知
     */
    EMAIL("邮件通知"),
    
    /**
     * App推送通知
     */
    PUSH("App推送"),
    
    /**
     * 微信通知
     */
    WECHAT("微信通知"),
    
    /**
     * 站内信
     */
    SITE_MESSAGE("站内信");
    
    private final String description;
    
    NotificationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}