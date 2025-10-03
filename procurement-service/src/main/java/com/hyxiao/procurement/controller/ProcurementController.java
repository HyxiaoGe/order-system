package com.hyxiao.procurement.controller;

import com.hyxiao.procurement.model.ProcurementOrder;
import com.hyxiao.procurement.service.ProcurementService;
import com.hyxiao.procurement.service.InventoryMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 采购控制器
 */
@RestController
@RequestMapping("/api/procurement")
@RequiredArgsConstructor
@Slf4j
public class ProcurementController {
    
    private final ProcurementService procurementService;
    private final InventoryMonitorService inventoryMonitorService;
    
    /**
     * 手动创建采购订单
     */
    @PostMapping("/orders")
    public ResponseEntity<ProcurementOrder> createProcurementOrder(@RequestBody CreateProcurementOrderRequest request) {
        log.info("接收创建采购订单请求: {}", request);
        
        try {
            ProcurementOrder order = procurementService.createManualProcurementOrder(
                    request.getProductId(),
                    request.getSupplierId(),
                    request.getQuantity(),
                    request.getRemark()
            );
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("创建采购订单失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 审批采购订单
     */
    @PostMapping("/orders/{orderId}/approve")
    public ResponseEntity<Map<String, Object>> approveProcurementOrder(
            @PathVariable String orderId,
            @RequestBody ApprovalRequest request) {
        log.info("接收审批采购订单请求: orderId={}, approver={}", orderId, request.getApprover());
        
        boolean success = procurementService.approveProcurementOrder(orderId, request.getApprover());
        
        Map<String, Object> response = Map.of(
                "success", success,
                "message", success ? "审批成功" : "审批失败"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 接收货物
     */
    @PostMapping("/orders/{orderId}/receive")
    public ResponseEntity<Map<String, Object>> receiveGoods(
            @PathVariable String orderId,
            @RequestBody ReceiveGoodsRequest request) {
        log.info("接收货物: orderId={}, receivedQuantity={}", orderId, request.getReceivedQuantity());
        
        boolean success = procurementService.receiveGoods(orderId, request.getReceivedQuantity());
        
        Map<String, Object> response = Map.of(
                "success", success,
                "message", success ? "接收成功" : "接收失败"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 手动触发库存检查
     */
    @PostMapping("/inventory/check")
    public ResponseEntity<Map<String, String>> triggerInventoryCheck() {
        log.info("接收手动库存检查请求");
        
        try {
            inventoryMonitorService.triggerInventoryCheck();
            return ResponseEntity.ok(Map.of("message", "库存检查已触发"));
        } catch (Exception e) {
            log.error("触发库存检查失败", e);
            return ResponseEntity.badRequest().body(Map.of("error", "触发失败"));
        }
    }
    
    /**
     * 检查特定产品库存
     */
    @PostMapping("/inventory/check/{productId}")
    public ResponseEntity<Map<String, String>> checkProductInventory(
            @PathVariable String productId,
            @RequestParam int currentStock) {
        log.info("接收产品库存检查请求: productId={}, currentStock={}", productId, currentStock);
        
        try {
            inventoryMonitorService.checkProductInventory(productId, currentStock);
            return ResponseEntity.ok(Map.of("message", "产品库存检查完成"));
        } catch (Exception e) {
            log.error("产品库存检查失败", e);
            return ResponseEntity.badRequest().body(Map.of("error", "检查失败"));
        }
    }
    
    // 请求DTO类
    public static class CreateProcurementOrderRequest {
        private String productId;
        private String supplierId;
        private Integer quantity;
        private String remark;
        
        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getSupplierId() { return supplierId; }
        public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        
        @Override
        public String toString() {
            return "CreateProcurementOrderRequest{" +
                    "productId='" + productId + '\'' +
                    ", supplierId='" + supplierId + '\'' +
                    ", quantity=" + quantity +
                    ", remark='" + remark + '\'' +
                    '}';
        }
    }
    
    public static class ApprovalRequest {
        private String approver;
        
        public String getApprover() { return approver; }
        public void setApprover(String approver) { this.approver = approver; }
    }
    
    public static class ReceiveGoodsRequest {
        private Integer receivedQuantity;
        
        public Integer getReceivedQuantity() { return receivedQuantity; }
        public void setReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    }
}