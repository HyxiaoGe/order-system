package com.hyxiao.procurement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 产品库存阈值配置实体类
 */
@Entity
@Table(name = "inventory_threshold")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryThreshold {
    
    @Id
    @Column(name = "product_id", length = 50)
    private String productId;
    
    @Column(name = "product_name", length = 200)
    private String productName;
    
    @Column(name = "min_threshold", nullable = false)
    private Integer minThreshold;
    
    @Column(name = "max_threshold", nullable = false)
    private Integer maxThreshold;
    
    @Column(name = "reorder_quantity", nullable = false)
    private Integer reorderQuantity; // 建议采购数量
    
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @Column(name = "remark", length = 500)
    private String remark;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}