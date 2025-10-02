package com.hyxiao.inventory.controller;

import com.hyxiao.common.dto.ApiResponse;
import com.hyxiao.inventory.model.Inventory;
import com.hyxiao.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 库存控制器
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    /**
     * 扣减库存
     */
    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<Boolean>> deductStock(@RequestParam String productId,
                                                           @RequestParam Integer quantity,
                                                           @RequestParam String orderId) {
        boolean success = inventoryService.deductStock(productId, quantity, orderId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("库存扣减成功", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("库存扣减失败"));
        }
    }
    
    /**
     * 释放库存
     */
    @PostMapping("/release")
    public ResponseEntity<ApiResponse<Boolean>> releaseStock(@RequestParam String productId,
                                                            @RequestParam Integer quantity,
                                                            @RequestParam String orderId) {
        boolean success = inventoryService.releaseStock(productId, quantity, orderId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("库存释放成功", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("库存释放失败"));
        }
    }
    
    /**
     * 查询库存信息
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<Inventory>> getInventory(@PathVariable String productId) {
        Optional<Inventory> inventory = inventoryService.findInventoryByProductId(productId);
        if (inventory.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(inventory.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 初始化库存（测试用）
     */
    @PostMapping("/init")
    public ResponseEntity<ApiResponse<Inventory>> initInventory(@RequestParam String productId,
                                                               @RequestParam String productName,
                                                               @RequestParam Integer totalStock) {
        Inventory inventory = inventoryService.initializeInventory(productId, productName, totalStock);
        return ResponseEntity.ok(ApiResponse.success("库存初始化成功", inventory));
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("库存服务运行正常"));
    }
}