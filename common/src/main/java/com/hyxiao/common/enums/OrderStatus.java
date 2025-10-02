package com.hyxiao.common.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    
    /**
     * 已创建，等待库存扣减
     */
    CREATED("已创建"),
    
    /**
     * 库存扣减成功，等待支付
     */
    INVENTORY_DEDUCTED("库存已扣减"),
    
    /**
     * 支付成功，等待通知
     */
    PAID("已支付"),
    
    /**
     * 订单完成
     */
    COMPLETED("已完成"),
    
    /**
     * 订单取消
     */
    CANCELLED("已取消");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}