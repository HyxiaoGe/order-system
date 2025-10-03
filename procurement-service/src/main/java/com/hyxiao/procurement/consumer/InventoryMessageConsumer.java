package com.hyxiao.procurement.consumer;

import com.hyxiao.procurement.event.InventoryCheckEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 库存消息消费者
 * 监听库存变化，发布库存检查事件
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryMessageConsumer implements MessageListener {
    
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    
    @Override
    public ConsumeResult consume(MessageView messageView) {
        try {
            String tag = messageView.getTag().orElse("");
            String body = new String(messageView.getBody().array());
            String messageId = messageView.getMessageId().toString();
            
            log.info("接收到库存消息: messageId={}, tag={}", messageId, tag);
            
            switch (tag) {
                case "inventory-deducted":
                    handleInventoryDeducted(body);
                    break;
                case "inventory-low-stock":
                    handleLowStockAlert(body);
                    break;
                default:
                    log.warn("未知的消息标签: tag={}", tag);
            }
            
            return ConsumeResult.SUCCESS;
            
        } catch (Exception e) {
            log.error("处理库存消息异常: messageId={}", messageView.getMessageId(), e);
            return ConsumeResult.FAILURE;
        }
    }
    
    /**
     * 处理库存扣减消息
     */
    private void handleInventoryDeducted(String messageBody) {
        try {
            log.info("处理库存扣减消息: {}", messageBody);
            
            // 解析消息内容
            var inventoryMessage = objectMapper.readValue(messageBody, InventoryDeductedMessage.class);
            
            // 发布库存检查事件
            InventoryCheckEvent event = new InventoryCheckEvent(
                    inventoryMessage.getProductId(),
                    inventoryMessage.getRemainingStock(),
                    InventoryCheckEvent.EventType.INVENTORY_DEDUCTED
            );
            eventPublisher.publishEvent(event);
            
        } catch (Exception e) {
            log.error("处理库存扣减消息失败", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 处理库存不足警告消息
     */
    private void handleLowStockAlert(String messageBody) {
        try {
            log.info("处理库存不足警告: {}", messageBody);
            
            // 解析消息内容
            var lowStockMessage = objectMapper.readValue(messageBody, LowStockMessage.class);
            
            // 发布库存检查事件
            InventoryCheckEvent event = new InventoryCheckEvent(
                    lowStockMessage.getProductId(),
                    lowStockMessage.getCurrentStock(),
                    InventoryCheckEvent.EventType.LOW_STOCK_ALERT
            );
            eventPublisher.publishEvent(event);
            
        } catch (Exception e) {
            log.error("处理库存不足警告失败", e);
            throw new RuntimeException(e);
        }
    }
    
    // 消息DTO类
    public static class InventoryDeductedMessage {
        private String productId;
        private Integer deductedQuantity;
        private Integer remainingStock;
        
        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public Integer getDeductedQuantity() { return deductedQuantity; }
        public void setDeductedQuantity(Integer deductedQuantity) { this.deductedQuantity = deductedQuantity; }
        
        public Integer getRemainingStock() { return remainingStock; }
        public void setRemainingStock(Integer remainingStock) { this.remainingStock = remainingStock; }
    }
    
    public static class LowStockMessage {
        private String productId;
        private Integer currentStock;
        private Integer threshold;
        
        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public Integer getCurrentStock() { return currentStock; }
        public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }
        
        public Integer getThreshold() { return threshold; }
        public void setThreshold(Integer threshold) { this.threshold = threshold; }
    }
}