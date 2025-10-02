package com.hyxiao.inventory.repository;

import com.hyxiao.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * 库存数据访问层
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {
    
    /**
     * 根据商品ID查询库存（悲观锁）
     * 防止并发扣减库存时的数据不一致
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") String productId);
    
    /**
     * 检查库存是否充足
     */
    @Query("SELECT CASE WHEN i.availableStock >= :quantity THEN true ELSE false END FROM Inventory i WHERE i.productId = :productId")
    boolean hasEnoughStock(@Param("productId") String productId, @Param("quantity") Integer quantity);
}