package com.hyxiao.ordersystem.controller;

import com.hyxiao.ordersystem.dto.ApiResponse;
import com.hyxiao.ordersystem.model.Inventory;
import com.hyxiao.ordersystem.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import java.util.Optional;

/**
 * 库存管理控制器
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    /**
     * 初始化商品库存（用于测试）
     */
    @PostMapping("/init")
    public ResponseEntity<ApiResponse<Inventory>> initInventory(
            @RequestParam @NotBlank String productId,
            @RequestParam @NotBlank String productName,
            @RequestParam @Min(1) Integer totalStock) {
        
        try {
            log.info("初始化商品库存: productId={}, productName={}, totalStock={}", 
                    productId, productName, totalStock);
            
            Inventory inventory = inventoryService.initializeInventory(productId, productName, totalStock);
            return ResponseEntity.ok(ApiResponse.success("库存初始化成功", inventory));
            
        } catch (Exception e) {
            log.error("初始化库存失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("库存初始化失败: " + e.getMessage()));
        }
    }
    
    /**
     * 查询商品库存
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<Inventory>> getInventory(@PathVariable String productId) {
        Optional<Inventory> inventoryOpt = inventoryService.findInventoryByProductId(productId);
        return inventoryOpt.map(inventory -> ResponseEntity.ok(ApiResponse.success(inventory))).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * 释放指定商品的锁定库存（用于测试回滚场景）
     */
    @PostMapping("/{productId}/release")
    public ResponseEntity<ApiResponse<String>> releaseInventory(
            @PathVariable String productId,
            @RequestParam @Min(1) Integer quantity,
            @RequestParam @NotBlank String orderId) {
        
        try {
            inventoryService.releaseInventory(productId, quantity, orderId);
            return ResponseEntity.ok(ApiResponse.success("库存释放成功"));
            
        } catch (Exception e) {
            log.error("释放库存失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("库存释放失败: " + e.getMessage()));
        }
    }
}