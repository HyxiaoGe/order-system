package com.hyxiao.ordersystem.service;

import com.hyxiao.ordersystem.dto.InventoryMessage;
import com.hyxiao.ordersystem.dto.OrderMessage;
import com.hyxiao.ordersystem.model.Inventory;
import com.hyxiao.ordersystem.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 库存服务层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final MessageProducerService messageProducerService;
    
    /**
     * 处理订单创建消息，执行库存扣减
     */
    @Transactional
    public void processOrderCreated(OrderMessage orderMessage) {
        log.info("开始处理订单库存扣减: orderId={}, productId={}, quantity={}", 
                orderMessage.getOrderId(), orderMessage.getProductId(), orderMessage.getQuantity());
        
        try {
            // 使用悲观锁查询库存
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdWithLock(orderMessage.getProductId());
            
            if (inventoryOpt.isEmpty()) {
                // 商品不存在
                log.warn("商品不存在: productId={}", orderMessage.getProductId());
                sendInventoryDeductionFailedMessage(orderMessage, "商品不存在");
                return;
            }
            
            Inventory inventory = inventoryOpt.get();
            
            // 检查库存是否充足
            if (!inventory.hasEnoughStock(orderMessage.getQuantity())) {
                log.warn("库存不足: productId={}, 需要数量={}, 可用库存={}", 
                        orderMessage.getProductId(), orderMessage.getQuantity(), inventory.getAvailableStock());
                sendInventoryDeductionFailedMessage(orderMessage, "库存不足");
                return;
            }
            
            // 扣减库存
            inventory.deductStock(orderMessage.getQuantity());
            inventoryRepository.save(inventory);
            
            log.info("库存扣减成功: orderId={}, productId={}, 扣减数量={}, 剩余可用库存={}", 
                    orderMessage.getOrderId(), orderMessage.getProductId(), 
                    orderMessage.getQuantity(), inventory.getAvailableStock());
            
            // 发送库存扣减成功消息
            sendInventoryDeductionSuccessMessage(orderMessage);
            
        } catch (Exception e) {
            log.error("库存扣减处理异常: orderId={}", orderMessage.getOrderId(), e);
            sendInventoryDeductionFailedMessage(orderMessage, "系统异常: " + e.getMessage());
            throw e; // 重新抛出异常，触发事务回滚
        }
    }
    
    /**
     * 释放库存（回滚操作）
     */
    @Transactional
    public void releaseInventory(String productId, Integer quantity, String orderId) {
        log.info("开始释放库存: orderId={}, productId={}, quantity={}", orderId, productId, quantity);
        
        try {
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdWithLock(productId);
            if (inventoryOpt.isPresent()) {
                Inventory inventory = inventoryOpt.get();
                inventory.releaseStock(quantity);
                inventoryRepository.save(inventory);
                
                log.info("库存释放成功: orderId={}, productId={}, 释放数量={}", orderId, productId, quantity);
            } else {
                log.warn("释放库存时商品不存在: productId={}", productId);
            }
        } catch (Exception e) {
            log.error("释放库存异常: orderId={}, productId={}", orderId, productId, e);
            throw e;
        }
    }
    
    /**
     * 发送库存扣减成功消息
     */
    private void sendInventoryDeductionSuccessMessage(OrderMessage orderMessage) {
        InventoryMessage inventoryMessage = InventoryMessage.builder()
                .orderId(orderMessage.getOrderId())
                .productId(orderMessage.getProductId())
                .quantity(orderMessage.getQuantity())
                .userId(orderMessage.getUserId())
                .amount(orderMessage.getAmount())
                .messageId(UUID.randomUUID().toString())
                .messageType("INVENTORY_DEDUCTED_SUCCESS")
                .processTime(LocalDateTime.now())
                .success(true)
                .build();
        
        messageProducerService.sendInventoryMessage(inventoryMessage, "inventory-success");
    }
    
    /**
     * 发送库存扣减失败消息
     */
    private void sendInventoryDeductionFailedMessage(OrderMessage orderMessage, String failureReason) {
        InventoryMessage inventoryMessage = InventoryMessage.builder()
                .orderId(orderMessage.getOrderId())
                .productId(orderMessage.getProductId())
                .quantity(orderMessage.getQuantity())
                .userId(orderMessage.getUserId())
                .amount(orderMessage.getAmount())
                .messageId(UUID.randomUUID().toString())
                .messageType("INVENTORY_DEDUCTED_FAILED")
                .processTime(LocalDateTime.now())
                .success(false)
                .failureReason(failureReason)
                .build();
        
        messageProducerService.sendInventoryMessage(inventoryMessage, "inventory-failed");
    }
    
    /**
     * 根据商品ID查询库存信息
     */
    public Optional<Inventory> findInventoryByProductId(String productId) {
        return inventoryRepository.findById(productId);
    }
    
    /**
     * 初始化商品库存（用于测试）
     */
    @Transactional
    public Inventory initializeInventory(String productId, String productName, Integer totalStock) {
        Inventory inventory = Inventory.builder()
                .productId(productId)
                .productName(productName)
                .totalStock(totalStock)
                .availableStock(totalStock)
                .lockedStock(0)
                .build();
        
        return inventoryRepository.save(inventory);
    }
}