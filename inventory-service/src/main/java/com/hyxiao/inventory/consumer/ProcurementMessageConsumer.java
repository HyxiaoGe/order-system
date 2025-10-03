package com.hyxiao.inventory.consumer;

import com.hyxiao.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 采购消息消费者
 * 处理采购服务发送的库存补货消息
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcurementMessageConsumer implements MessageListener {
    
    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;
    
    @Override
    public ConsumeResult consume(MessageView messageView) {
        try {
            String tag = messageView.getTag().orElse("");
            String body = new String(messageView.getBody().array());
            String messageId = messageView.getMessageId().toString();
            
            log.info("接收到采购消息: messageId={}, tag={}", messageId, tag);
            
            switch (tag) {
                case "inventory-restock":
                    handleInventoryRestock(body);
                    break;
                default:
                    log.warn("未知的消息标签: tag={}", tag);
            }
            
            return ConsumeResult.SUCCESS;
            
        } catch (Exception e) {
            log.error("处理采购消息异常: messageId={}", messageView.getMessageId(), e);
            return ConsumeResult.FAILURE;
        }
    }
    
    /**
     * 处理库存补货消息
     */
    private void handleInventoryRestock(String messageBody) {
        try {
            log.info("处理库存补货消息: {}", messageBody);
            
            // 解析消息内容
            var restockMessage = objectMapper.readValue(messageBody, RestockMessage.class);
            
            // 执行库存补货
            boolean success = inventoryService.addStock(
                    restockMessage.getProductId(),
                    restockMessage.getQuantity(),
                    restockMessage.getProcurementOrderId()
            );
            
            if (success) {
                log.info("库存补货成功: productId={}, quantity={}", 
                        restockMessage.getProductId(), restockMessage.getQuantity());
            } else {
                log.error("库存补货失败: productId={}, quantity={}", 
                        restockMessage.getProductId(), restockMessage.getQuantity());
            }
            
        } catch (Exception e) {
            log.error("处理库存补货消息失败", e);
            throw new RuntimeException(e);
        }
    }
    
    // 消息DTO类
    public static class RestockMessage {
        private String productId;
        private Integer quantity;
        private String procurementOrderId;
        private LocalDateTime restockTime;
        
        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getProcurementOrderId() { return procurementOrderId; }
        public void setProcurementOrderId(String procurementOrderId) { this.procurementOrderId = procurementOrderId; }
        
        public LocalDateTime getRestockTime() { return restockTime; }
        public void setRestockTime(LocalDateTime restockTime) { this.restockTime = restockTime; }
    }
}