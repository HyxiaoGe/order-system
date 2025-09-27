package com.hyxiao.ordersystem.model;

/**
 * 支付状态枚举
 */
public enum PaymentStatus {
    
    /**
     * 支付处理中
     */
    PROCESSING("处理中"),
    
    /**
     * 支付成功
     */
    SUCCESS("支付成功"),
    
    /**
     * 支付失败
     */
    FAILED("支付失败"),
    
    /**
     * 支付超时
     */
    TIMEOUT("支付超时"),
    
    /**
     * 支付取消
     */
    CANCELLED("支付取消");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}