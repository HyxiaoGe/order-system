package com.hyxiao.procurement.service;

import com.hyxiao.procurement.dto.InventoryInfoDTO;
import com.hyxiao.procurement.client.InventoryServiceClient;
import com.hyxiao.procurement.repository.InventoryThresholdRepository;
import com.hyxiao.procurement.model.InventoryThreshold;
import com.hyxiao.procurement.event.InventoryCheckEvent;
import com.hyxiao.procurement.event.ProcurementCheckEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 库存监控服务
 * 定期检查库存水平并触发采购流程
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryMonitorService {
    
    private final InventoryThresholdRepository inventoryThresholdRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final ApplicationEventPublisher eventPublisher;
    
    // 库存阈值配置（实际应该从配置中心获取）
    private static final int DEFAULT_THRESHOLD = 10;
    
    /**
     * 定时检查库存水平
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void monitorInventoryLevels() {
        log.info("开始库存监控检查");
        
        try {
            // 从库存服务获取所有产品的库存信息
            List<InventoryInfoDTO> inventoryList = fetchAllInventoryFromService();
            
            for (InventoryInfoDTO inventory : inventoryList) {
                // 发布库存检查事件
                InventoryCheckEvent event = new InventoryCheckEvent(
                        inventory.getProductId(),
                        inventory.getAvailableStock(),
                        InventoryCheckEvent.EventType.MANUAL_CHECK
                );
                eventPublisher.publishEvent(event);
            }
            
            log.info("库存监控检查完成，共检查了 {} 个产品", inventoryList.size());
        } catch (Exception e) {
            log.error("库存监控检查异常", e);
        }
    }
    
    /**
     * 从库存服务获取所有产品库存信息
     */
    private List<InventoryInfoDTO> fetchAllInventoryFromService() {
        try {
            log.debug("从库存服务获取库存信息");
            return inventoryServiceClient.getAllInventory();
        } catch (Exception e) {
            log.error("获取库存信息失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 监听库存检查事件
     */
    @EventListener
    public void handleInventoryCheckEvent(InventoryCheckEvent event) {
        log.info("接收到库存检查事件: productId={}, currentStock={}, eventType={}", 
                event.getProductId(), event.getCurrentStock(), event.getEventType());
        
        checkProductInventory(event.getProductId(), event.getCurrentStock());
    }
    
    /**
     * 检查特定产品库存
     */
    public void checkProductInventory(String productId, int currentStock) {
        log.debug("检查产品库存: productId={}, currentStock={}", productId, currentStock);
        
        int threshold = getInventoryThreshold(productId);
        
        if (currentStock <= threshold) {
            log.warn("产品库存低于阈值: productId={}, currentStock={}, threshold={}", 
                    productId, currentStock, threshold);
            
            // 发布采购检查事件
            ProcurementCheckEvent procurementEvent = new ProcurementCheckEvent(
                    productId, 
                    currentStock, 
                    threshold, 
                    "库存低于阈值，需要补货"
            );
            eventPublisher.publishEvent(procurementEvent);
        }
    }
    
    /**
     * 获取产品库存阈值
     */
    private int getInventoryThreshold(String productId) {
        try {
            // 从数据库获取产品特定的阈值配置
            Optional<InventoryThreshold> thresholdOpt = inventoryThresholdRepository
                    .findByProductIdAndIsEnabledTrue(productId);
            
            if (thresholdOpt.isPresent()) {
                InventoryThreshold threshold = thresholdOpt.get();
                log.debug("使用产品专用阈值: productId={}, threshold={}", 
                        productId, threshold.getMinThreshold());
                return threshold.getMinThreshold();
            } else {
                log.debug("产品未配置阈值，使用默认值: productId={}, defaultThreshold={}", 
                        productId, DEFAULT_THRESHOLD);
                return DEFAULT_THRESHOLD;
            }
        } catch (Exception e) {
            log.error("获取产品阈值失败，使用默认值: productId={}", productId, e);
            return DEFAULT_THRESHOLD;
        }
    }
    
    /**
     * 手动触发库存检查
     */
    public void triggerInventoryCheck() {
        log.info("手动触发库存检查");
        monitorInventoryLevels();
    }
}