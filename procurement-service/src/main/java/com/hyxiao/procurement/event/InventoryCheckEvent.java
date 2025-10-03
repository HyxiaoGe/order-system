package com.hyxiao.procurement.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 库存检查事件
 * 当收到库存消息或需要检查库存时发布此事件
 */
@Data
@AllArgsConstructor
public class InventoryCheckEvent {
    
    /**
     * 产品ID
     */
    private String productId;
    
    /**
     * 当前库存数量
     */
    private Integer currentStock;
    
    /**
     * 事件类型
     */
    private EventType eventType;
    
    /**
     * 事件类型枚举
     */
    public enum EventType {
        INVENTORY_DEDUCTED,    // 库存扣减
        LOW_STOCK_ALERT,       // 库存不足警告
        MANUAL_CHECK           // 手动检查
    }
}