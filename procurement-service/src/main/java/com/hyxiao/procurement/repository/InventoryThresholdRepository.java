package com.hyxiao.procurement.repository;

import com.hyxiao.procurement.model.InventoryThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存阈值数据访问层
 */
@Repository
public interface InventoryThresholdRepository extends JpaRepository<InventoryThreshold, String> {
    
    /**
     * 根据产品ID查询阈值配置
     */
    Optional<InventoryThreshold> findByProductIdAndIsEnabledTrue(String productId);
    
    /**
     * 检查产品是否有阈值配置
     */
    boolean existsByProductIdAndIsEnabledTrue(String productId);
}