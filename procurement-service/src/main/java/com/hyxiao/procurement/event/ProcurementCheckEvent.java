package com.hyxiao.procurement.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 采购检查事件
 * 当库存低于阈值时发布此事件
 */
@Data
@AllArgsConstructor
public class ProcurementCheckEvent {
    
    /**
     * 产品ID
     */
    private String productId;
    
    /**
     * 当前库存数量
     */
    private Integer currentStock;
    
    /**
     * 库存阈值
     */
    private Integer threshold;
    
    /**
     * 检查原因
     */
    private String reason;
}