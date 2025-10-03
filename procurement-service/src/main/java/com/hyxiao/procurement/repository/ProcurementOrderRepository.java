package com.hyxiao.procurement.repository;

import com.hyxiao.procurement.model.ProcurementOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单数据访问层
 */
@Repository
public interface ProcurementOrderRepository extends JpaRepository<ProcurementOrder, String> {
    
    /**
     * 根据状态查询采购订单
     */
    List<ProcurementOrder> findByStatus(ProcurementOrder.ProcurementOrderStatus status);
    
    /**
     * 根据供应商ID查询采购订单
     */
    List<ProcurementOrder> findBySupplierId(String supplierId);
    
    /**
     * 根据产品ID查询采购订单
     */
    List<ProcurementOrder> findByProductId(String productId);
    
    /**
     * 查询指定时间范围内的采购订单
     */
    List<ProcurementOrder> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询需要处理的采购订单（已审批或在途）
     */
    @Query("SELECT p FROM ProcurementOrder p WHERE p.status IN ('APPROVED', 'IN_TRANSIT', 'PARTIALLY_RECEIVED')")
    List<ProcurementOrder> findPendingReceiptOrders();
    
    /**
     * 根据产品ID和状态查询采购中的数量
     */
    @Query("SELECT SUM(p.quantity - p.receivedQuantity) FROM ProcurementOrder p " +
           "WHERE p.productId = :productId AND p.status IN ('APPROVED', 'IN_TRANSIT', 'PARTIALLY_RECEIVED')")
    Integer findProcuringQuantityByProductId(@Param("productId") String productId);
}