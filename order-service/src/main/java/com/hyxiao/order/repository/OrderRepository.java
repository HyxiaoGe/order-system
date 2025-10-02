package com.hyxiao.order.repository;

import com.hyxiao.order.model.Order;
import com.hyxiao.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单数据访问层
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    /**
     * 根据用户ID查询订单
     */
    List<Order> findByUserId(String userId);
    
    /**
     * 根据状态查询订单
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * 查询指定时间之前创建的订单
     */
    List<Order> findByCreateTimeBeforeAndStatus(LocalDateTime createTime, OrderStatus status);
}