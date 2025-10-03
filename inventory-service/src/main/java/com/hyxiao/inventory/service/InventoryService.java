package com.hyxiao.inventory.service;

import com.hyxiao.common.dto.InventoryMessage;
import com.hyxiao.common.dto.OrderMessage;
import com.hyxiao.inventory.model.Inventory;
import com.hyxiao.inventory.repository.InventoryRepository;
import com.hyxiao.inventory.dto.InventoryInfoDTO;
import com.hyxiao.common.message.template.MessageTemplate;
import com.hyxiao.common.message.core.MessageSendResult;
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
    private final MessageTemplate messageTemplate;
    
    /**
     * 处理订单创建消息，执行库存扣减
     */
    @Transactional
    public boolean processOrderCreated(OrderMessage orderMessage) {
        log.info("开始处理订单库存扣减: orderId={}, productId={}, quantity={}", 
                orderMessage.getOrderId(), orderMessage.getProductId(), orderMessage.getQuantity());
        
        try {
            // 使用悲观锁查询库存
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdWithLock(orderMessage.getProductId());
            
            if (inventoryOpt.isEmpty()) {
                log.warn("商品不存在: productId={}", orderMessage.getProductId());
                return false;
            }
            
            Inventory inventory = inventoryOpt.get();
            
            // 检查库存是否充足
            if (!inventory.hasEnoughStock(orderMessage.getQuantity())) {
                log.warn("库存不足: productId={}, 需要数量={}, 可用库存={}", 
                        orderMessage.getProductId(), orderMessage.getQuantity(), inventory.getAvailableStock());
                return false;
            }
            
            // 扣减库存
            inventory.deductStock(orderMessage.getQuantity());
            inventoryRepository.save(inventory);
            
            log.info("库存扣减成功: orderId={}, productId={}, 扣减数量={}, 剩余可用库存={}", 
                    orderMessage.getOrderId(), orderMessage.getProductId(), 
                    orderMessage.getQuantity(), inventory.getAvailableStock());
            
            // 发送库存扣减成功消息
            sendInventoryDeductionSuccessMessage(orderMessage);
            
            return true;
            
        } catch (Exception e) {
            log.error("库存扣减处理异常: orderId={}", orderMessage.getOrderId(), e);
            throw e;
        }
    }
    
    /**
     * 扣减库存 - 提供给其他服务调用
     */
    @Transactional
    public boolean deductStock(String productId, Integer quantity, String orderId) {
        log.info("扣减库存: orderId={}, productId={}, quantity={}", orderId, productId, quantity);
        
        try {
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdWithLock(productId);
            
            if (inventoryOpt.isEmpty()) {
                log.warn("商品不存在: productId={}", productId);
                return false;
            }
            
            Inventory inventory = inventoryOpt.get();
            
            if (!inventory.hasEnoughStock(quantity)) {
                log.warn("库存不足: productId={}, 需要数量={}, 可用库存={}", 
                        productId, quantity, inventory.getAvailableStock());
                return false;
            }
            
            inventory.deductStock(quantity);
            inventoryRepository.save(inventory);
            
            log.info("库存扣减成功: orderId={}, productId={}, 扣减数量={}", orderId, productId, quantity);
            return true;
            
        } catch (Exception e) {
            log.error("库存扣减异常: orderId={}, productId={}", orderId, productId, e);
            return false;
        }
    }
    
    /**
     * 释放库存（回滚操作）
     */
    @Transactional
    public boolean releaseStock(String productId, Integer quantity, String orderId) {
        log.info("开始释放库存: orderId={}, productId={}, quantity={}", orderId, productId, quantity);
        
        try {
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdWithLock(productId);
            if (inventoryOpt.isPresent()) {
                Inventory inventory = inventoryOpt.get();
                inventory.releaseStock(quantity);
                inventoryRepository.save(inventory);
                
                log.info("库存释放成功: orderId={}, productId={}, 释放数量={}", orderId, productId, quantity);
                return true;
            } else {
                log.warn("释放库存时商品不存在: productId={}", productId);
                return false;
            }
        } catch (Exception e) {
            log.error("释放库存异常: orderId={}, productId={}", orderId, productId, e);
            return false;
        }
    }
    
    /**
     * 根据商品ID查询库存信息
     */
    public Optional<Inventory> findInventoryByProductId(String productId) {
        return inventoryRepository.findById(productId);
    }
    
    /**
     * 增加库存（补货）
     */
    @Transactional
    public boolean addStock(String productId, Integer quantity, String procurementOrderId) {
        log.info("库存补货: productId={}, quantity={}, procurementOrderId={}", 
                productId, quantity, procurementOrderId);
        
        try {
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdWithLock(productId);
            
            if (inventoryOpt.isEmpty()) {
                log.warn("商品不存在，无法补货: productId={}", productId);
                return false;
            }
            
            Inventory inventory = inventoryOpt.get();
            inventory.addStock(quantity);
            inventoryRepository.save(inventory);
            
            log.info("库存补货成功: productId={}, 补货数量={}, 当前库存={}", 
                    productId, quantity, inventory.getAvailableStock());
            
            return true;
            
        } catch (Exception e) {
            log.error("库存补货异常: productId={}, procurementOrderId={}", productId, procurementOrderId, e);
            return false;
        }
    }
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
        
        messageTemplate.sendInventoryDeductSuccess(inventoryMessage.getOrderId(), inventoryMessage);
    }
    
    /**
     * 获取所有产品的库存信息
     */
    public java.util.List<InventoryInfoDTO> getAllInventoryInfo() {
        log.info("获取所有产品库存信息");
        
        try {
            return inventoryRepository.findAll().stream()
                .map(inventory -> new InventoryInfoDTO(
                    inventory.getProductId(),
                    inventory.getProductName(),
                    inventory.getAvailableStock(),
                    inventory.getTotalStock(),
                    inventory.getLockedStock()
                ))
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("获取所有库存信息失败", e);
            return java.util.Collections.emptyList();
        }
    }
    
    /**
     * 根据产品ID获取库存信息
     */
    public Optional<InventoryInfoDTO> getInventoryInfoByProductId(String productId) {
        log.info("获取产品库存信息: productId={}", productId);
        
        try {
            Optional<Inventory> inventoryOpt = inventoryRepository.findById(productId);
            return inventoryOpt.map(inventory -> 
                new InventoryInfoDTO(
                    inventory.getProductId(),
                    inventory.getProductName(),
                    inventory.getAvailableStock(),
                    inventory.getTotalStock(),
                    inventory.getLockedStock()
                )
            );
        } catch (Exception e) {
            log.error("获取产品库存信息失败: productId={}", productId, e);
            return Optional.empty();
        }
    }
}