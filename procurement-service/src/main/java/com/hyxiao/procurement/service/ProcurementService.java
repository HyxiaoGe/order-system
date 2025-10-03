package com.hyxiao.procurement.service;

import com.hyxiao.procurement.model.ProcurementOrder;
import com.hyxiao.procurement.model.ProductSupplier;
import com.hyxiao.procurement.model.Supplier;
import com.hyxiao.procurement.repository.ProcurementOrderRepository;
import com.hyxiao.procurement.repository.ProductSupplierRepository;
import com.hyxiao.procurement.repository.SupplierRepository;
import com.hyxiao.procurement.event.ProcurementCheckEvent;
import com.hyxiao.common.message.template.MessageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 采购服务层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcurementService {
    
    private final ProcurementOrderRepository procurementOrderRepository;
    private final ProductSupplierRepository productSupplierRepository;
    private final SupplierRepository supplierRepository;
    private final MessageTemplate messageTemplate;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * 监听采购检查事件
     */
    @EventListener
    public void handleProcurementCheckEvent(ProcurementCheckEvent event) {
        log.info("接收到采购检查事件: productId={}, currentStock={}, threshold={}, reason={}", 
                event.getProductId(), event.getCurrentStock(), event.getThreshold(), event.getReason());
        
        checkInventoryAndCreateProcurementPlan(
                event.getProductId(), 
                event.getCurrentStock(), 
                event.getThreshold()
        );
    }
    
    /**
     * 检查库存并自动生成采购计划
     */
    @Transactional
    public void checkInventoryAndCreateProcurementPlan(String productId, int currentStock, int threshold) {
        log.info("检查库存并生成采购计划: productId={}, currentStock={}, threshold={}", 
                productId, currentStock, threshold);
        
        if (currentStock <= threshold) {
            // 计算需要采购的数量
            int needQuantity = calculateProcurementQuantity(productId, currentStock, threshold);
            
            // 查找最佳供应商
            Optional<ProductSupplier> bestSupplier = findBestSupplier(productId);
            
            if (bestSupplier.isPresent()) {
                createProcurementOrder(productId, needQuantity, bestSupplier.get());
            } else {
                log.warn("找不到产品的供应商: productId={}", productId);
            }
        }
    }
    
    /**
     * 创建采购订单
     */
    @Transactional
    public ProcurementOrder createProcurementOrder(String productId, int quantity, ProductSupplier productSupplier) {
        log.info("创建采购订单: productId={}, quantity={}, supplierId={}", 
                productId, quantity, productSupplier.getSupplierId());
        
        // 检查最小订购量
        if (quantity < productSupplier.getMinOrderQuantity()) {
            quantity = productSupplier.getMinOrderQuantity();
            log.info("调整采购数量至最小订购量: {}", quantity);
        }
        
        String orderId = "PO-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        BigDecimal totalAmount = productSupplier.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
        
        ProcurementOrder order = ProcurementOrder.builder()
                .orderId(orderId)
                .supplierId(productSupplier.getSupplierId())
                .productId(productId)
                .productName(getProductName(productId)) // 从数据库或服务获取
                .quantity(quantity)
                .unitPrice(productSupplier.getUnitPrice())
                .totalAmount(totalAmount)
                .status(ProcurementOrder.ProcurementOrderStatus.PENDING)
                .expectedDeliveryDate(LocalDateTime.now().plusDays(productSupplier.getLeadTimeDays()))
                .remark("系统自动创建")
                .build();
        
        ProcurementOrder savedOrder = procurementOrderRepository.save(order);
        
        // 发送采购订单创建消息
        sendProcurementOrderCreatedMessage(savedOrder);
        
        log.info("采购订单创建成功: orderId={}", orderId);
        return savedOrder;
    }
    
    /**
     * 手动创建采购订单
     */
    @Transactional
    public ProcurementOrder createManualProcurementOrder(String productId, String supplierId, 
                                                        int quantity, String remark) {
        log.info("手动创建采购订单: productId={}, supplierId={}, quantity={}", 
                productId, supplierId, quantity);
        
        // 查找产品供应商信息
        Optional<ProductSupplier> productSupplier = productSupplierRepository
                .findByProductIdAndSupplierId(productId, supplierId);
        
        if (productSupplier.isEmpty()) {
            throw new IllegalArgumentException("找不到产品供应商关系");
        }
        
        return createProcurementOrder(productId, quantity, productSupplier.get());
    }
    
    /**
     * 审批采购订单
     */
    @Transactional
    public boolean approveProcurementOrder(String orderId, String approver) {
        log.info("审批采购订单: orderId={}, approver={}", orderId, approver);
        
        Optional<ProcurementOrder> orderOpt = procurementOrderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            log.warn("采购订单不存在: orderId={}", orderId);
            return false;
        }
        
        ProcurementOrder order = orderOpt.get();
        if (order.getStatus() != ProcurementOrder.ProcurementOrderStatus.PENDING) {
            log.warn("采购订单状态不允许审批: orderId={}, currentStatus={}", orderId, order.getStatus());
            return false;
        }
        
        order.setStatus(ProcurementOrder.ProcurementOrderStatus.APPROVED);
        procurementOrderRepository.save(order);
        
        // 发送审批通过消息
        sendProcurementOrderApprovedMessage(order);
        
        log.info("采购订单审批成功: orderId={}", orderId);
        return true;
    }
    
    /**
     * 货物到达，接收货物
     */
    @Transactional
    public boolean receiveGoods(String orderId, int receivedQuantity) {
        log.info("接收货物: orderId={}, receivedQuantity={}", orderId, receivedQuantity);
        
        Optional<ProcurementOrder> orderOpt = procurementOrderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            log.warn("采购订单不存在: orderId={}", orderId);
            return false;
        }
        
        ProcurementOrder order = orderOpt.get();
        if (!order.canReceiveGoods()) {
            log.warn("采购订单状态不允许接收货物: orderId={}, currentStatus={}", orderId, order.getStatus());
            return false;
        }
        
        // 更新采购订单状态
        order.receiveGoods(receivedQuantity);
        procurementOrderRepository.save(order);
        
        // 发送库存入库消息
        sendInventoryRestockMessage(order, receivedQuantity);
        
        log.info("货物接收成功: orderId={}, receivedQuantity={}, totalReceived={}", 
                orderId, receivedQuantity, order.getReceivedQuantity());
        
        return true;
    }
    
    /**
     * 查找产品的最佳供应商
     */
    private Optional<ProductSupplier> findBestSupplier(String productId) {
        // 首先查找首选供应商
        Optional<ProductSupplier> preferredSupplier = productSupplierRepository
                .findByProductIdAndIsPreferredTrue(productId);
        
        if (preferredSupplier.isPresent()) {
            return preferredSupplier;
        }
        
        // 如果没有首选供应商，按评分选择最佳供应商
        List<ProductSupplier> suppliers = productSupplierRepository
                .findByProductIdOrderByRating(productId);
        
        return suppliers.isEmpty() ? Optional.empty() : Optional.of(suppliers.get(0));
    }
    
    /**
     * 计算采购数量
     */
    private int calculateProcurementQuantity(String productId, int currentStock, int threshold) {
        // 基础采购量 = 阈值的2倍 - 当前库存
        int baseQuantity = threshold * 2 - currentStock;
        
        // 考虑正在采购中的数量
        Integer procuringQuantity = procurementOrderRepository.findProcuringQuantityByProductId(productId);
        if (procuringQuantity != null) {
            baseQuantity -= procuringQuantity;
        }
        
        return Math.max(baseQuantity, 0);
    }
    
    /**
     * 获取产品名称（从数据库或服务获取）
     */
    private String getProductName(String productId) {
        try {
            // 从 ProductSupplier 表中获取产品信息
            Optional<ProductSupplier> productSupplier = productSupplierRepository
                    .findByProductId(productId)
                    .stream()
                    .findFirst();
            
            if (productSupplier.isPresent()) {
                log.debug("从 ProductSupplier 表中找到产品: productId={}", productId);
            }
            
            log.warn("未找到产品名称，使用默认命名: productId={}", productId);
            return generateDefaultProductName(productId);
            
        } catch (Exception e) {
            log.error("获取产品名称失败，使用默认名称: productId={}", productId, e);
            return generateDefaultProductName(productId);
        }
    }
    
    /**
     * 生成默认产品名称
     */
    private String generateDefaultProductName(String productId) {
        // 根据产品ID生成有意义的名称
        if (productId.startsWith("PRODUCT-")) {
            String number = productId.replace("PRODUCT-", "");
            return "商品" + number;
        } else {
            return "产品-" + productId;
        }
    }
    
    /**
     * 发送采购订单创建消息
     */
    private void sendProcurementOrderCreatedMessage(ProcurementOrder order) {
        messageTemplate.sendProcurementOrderCreated(order.getOrderId(), order);
    }
    
    /**
     * 发送采购订单审批消息
     */
    private void sendProcurementOrderApprovedMessage(ProcurementOrder order) {
        messageTemplate.sendProcurementOrderApproved(order.getOrderId(), order);
    }
    
    /**
     * 发送库存补货消息
     */
    private void sendInventoryRestockMessage(ProcurementOrder order, int receivedQuantity) {
        // 构建库存补货消息
        var restockMessage = new Object() {
            public final String productId = order.getProductId();
            public final int quantity = receivedQuantity;
            public final String procurementOrderId = order.getOrderId();
            public final LocalDateTime restockTime = LocalDateTime.now();
        };
        
        messageTemplate.sendInventoryRestock(order.getProductId(), restockMessage);
    }
}