package com.e.commerce.mini.Repository;

import com.e.commerce.mini.models.Order;
import com.e.commerce.mini.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    void deleteByOrder(Order order);
}