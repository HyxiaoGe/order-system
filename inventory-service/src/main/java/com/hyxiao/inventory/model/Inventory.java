package com.hyxiao.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 库存实体类
 */
@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    
    @Id
    @Column(name = "product_id", length = 50)
    private String productId;
    
    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;
    
    @Column(name = "total_stock", nullable = false)
    private Integer totalStock;
    
    @Column(name = "available_stock", nullable = false)
    private Integer availableStock;
    
    @Column(name = "locked_stock", nullable = false)
    private Integer lockedStock;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    /**
     * 检查是否有足够的可用库存
     */
    public boolean hasEnoughStock(int quantity) {
        return availableStock >= quantity;
    }
    
    /**
     * 扣减库存
     */
    public void deductStock(int quantity) {
        if (!hasEnoughStock(quantity)) {
            throw new IllegalArgumentException("库存不足，无法扣减");
        }
        this.availableStock -= quantity;
        this.lockedStock += quantity;
    }
    
    /**
     * 释放锁定的库存（回滚）
     */
    public void releaseStock(int quantity) {
        if (lockedStock < quantity) {
            throw new IllegalArgumentException("锁定库存不足，无法释放");
        }
        this.lockedStock -= quantity;
        this.availableStock += quantity;
    }
    
    /**
     * 确认库存扣减（最终扣减）
     */
    public void confirmDeduction(int quantity) {
        if (lockedStock < quantity) {
            throw new IllegalArgumentException("锁定库存不足，无法确认扣减");
        }
        this.lockedStock -= quantity;
        this.totalStock -= quantity;
    }
}