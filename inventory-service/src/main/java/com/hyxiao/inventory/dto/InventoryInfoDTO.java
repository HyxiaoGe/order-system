package com.hyxiao.inventory.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 库存信息传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryInfoDTO {
    
    private String productId;
    private String productName;
    private Integer availableStock;
    private Integer totalStock;
    private Integer lockedStock;
    
    /**
     * 构造器 - 不包含锁定库存
     */
    public InventoryInfoDTO(String productId, String productName, Integer availableStock, Integer totalStock) {
        this.productId = productId;
        this.productName = productName;
        this.availableStock = availableStock;
        this.totalStock = totalStock;
        this.lockedStock = 0;
    }
}