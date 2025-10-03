package com.hyxiao.procurement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品供应信息实体类
 */
@Entity
@Table(name = "product_supplier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSupplier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", length = 50, nullable = false)
    private String productId;
    
    @Column(name = "supplier_id", length = 50, nullable = false)
    private String supplierId;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "min_order_quantity", nullable = false)
    private Integer minOrderQuantity;
    
    @Column(name = "lead_time_days", nullable = false)
    private Integer leadTimeDays; // 供货周期（天）
    
    @Column(name = "is_preferred", nullable = false)
    private Boolean isPreferred = false; // 是否为首选供应商
    
    @Column(name = "quality_rating")
    private Integer qualityRating; // 质量评级 1-5
    
    @Column(name = "delivery_rating")
    private Integer deliveryRating; // 交付评级 1-5
    
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
     * 计算综合评分
     */
    public double getOverallRating() {
        if (qualityRating == null || deliveryRating == null) {
            return 0.0;
        }
        // 质量占权重60%，交付占权重40%
        return qualityRating * 0.6 + deliveryRating * 0.4;
    }
}