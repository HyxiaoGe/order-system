package com.hyxiao.procurement.repository;

import com.hyxiao.procurement.model.ProductSupplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 产品供应信息数据访问层
 */
@Repository
public interface ProductSupplierRepository extends JpaRepository<ProductSupplier, Long> {
    
    /**
     * 根据产品ID查询供应商信息
     */
    List<ProductSupplier> findByProductId(String productId);
    
    /**
     * 根据供应商ID查询供应的产品信息
     */
    List<ProductSupplier> findBySupplierId(String supplierId);
    
    /**
     * 查询产品的首选供应商
     */
    Optional<ProductSupplier> findByProductIdAndIsPreferredTrue(String productId);
    
    /**
     * 根据产品ID查询所有供应商，按综合评分排序
     */
    @Query("SELECT ps FROM ProductSupplier ps WHERE ps.productId = :productId " +
           "ORDER BY (ps.qualityRating * 0.6 + ps.deliveryRating * 0.4) DESC")
    List<ProductSupplier> findByProductIdOrderByRating(@Param("productId") String productId);
    
    /**
     * 查询特定产品和供应商的关系
     */
    Optional<ProductSupplier> findByProductIdAndSupplierId(String productId, String supplierId);
}