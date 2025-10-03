package com.hyxiao.procurement.repository;

import com.hyxiao.procurement.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 供应商数据访问层
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {
    
    /**
     * 根据状态查询供应商
     */
    List<Supplier> findByStatus(Supplier.SupplierStatus status);
    
    /**
     * 根据供应商名称查询
     */
    Optional<Supplier> findBySupplierName(String supplierName);
    
    /**
     * 查询活跃的供应商
     */
    @Query("SELECT s FROM Supplier s WHERE s.status = 'ACTIVE'")
    List<Supplier> findActiveSuppliers();
    
    /**
     * 根据信用评级查询供应商
     */
    List<Supplier> findByCreditRatingGreaterThanEqual(Integer creditRating);
}