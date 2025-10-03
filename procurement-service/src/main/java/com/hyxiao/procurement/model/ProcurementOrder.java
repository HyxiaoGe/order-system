package com.hyxiao.procurement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单实体类
 */
@Entity
@Table(name = "procurement_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementOrder {
    
    @Id
    @Column(name = "order_id", length = 50)
    private String orderId;
    
    @Column(name = "supplier_id", length = 50, nullable = false)
    private String supplierId;
    
    @Column(name = "product_id", length = 50, nullable = false)
    private String productId;
    
    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcurementOrderStatus status = ProcurementOrderStatus.PENDING;
    
    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;
    
    @Column(name = "received_quantity")
    private Integer receivedQuantity = 0;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @Column(name = "remark", length = 1000)
    private String remark;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (totalAmount == null && unitPrice != null && quantity != null) {
            totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    /**
     * 检查是否可以接收货物
     */
    public boolean canReceiveGoods() {
        return status == ProcurementOrderStatus.APPROVED || 
               status == ProcurementOrderStatus.IN_TRANSIT;
    }
    
    /**
     * 接收货物
     */
    public void receiveGoods(int receivedQty) {
        if (!canReceiveGoods()) {
            throw new IllegalStateException("当前状态不允许接收货物");
        }
        
        this.receivedQuantity += receivedQty;
        this.actualDeliveryDate = LocalDateTime.now();
        
        if (this.receivedQuantity >= this.quantity) {
            this.status = ProcurementOrderStatus.COMPLETED;
        } else {
            this.status = ProcurementOrderStatus.PARTIALLY_RECEIVED;
        }
    }
    
    public enum ProcurementOrderStatus {
        PENDING,             // 待审批
        APPROVED,            // 已审批
        REJECTED,            // 已拒绝
        IN_TRANSIT,          // 在途
        PARTIALLY_RECEIVED,  // 部分到货
        COMPLETED,           // 已完成
        CANCELLED            // 已取消
    }
}