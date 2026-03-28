package com.pollinate.order.management.repository;

import com.pollinate.order.management.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
