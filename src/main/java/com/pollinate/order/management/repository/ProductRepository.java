package com.pollinate.order.management.repository;

import com.pollinate.order.management.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
