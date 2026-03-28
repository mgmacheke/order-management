package com.pollinate.order.management.service;

import com.pollinate.order.management.dto.OrderItemRequest;
import com.pollinate.order.management.dto.OrderRequest;
import com.pollinate.order.management.dto.OrderResponse;
import com.pollinate.order.management.exception.OrderValidationException;
import com.pollinate.order.management.exception.ResourceNotFoundException;
import com.pollinate.order.management.model.Order;
import com.pollinate.order.management.model.OrderItem;
import com.pollinate.order.management.model.Product;
import com.pollinate.order.management.repository.OrderRepository;
import com.pollinate.order.management.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new OrderValidationException("Order must contain at least one item");
        }

        List<Long> requestedIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> productMap = productRepository.findAllById(requestedIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Long> missingIds = requestedIds.stream()
                .filter(id -> !productMap.containsKey(id))
                .collect(Collectors.toList());

        if (!missingIds.isEmpty()) {
            throw new OrderValidationException("Products not found with IDs: " + missingIds);
        }

        Order order = new Order();
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(productMap.get(itemReq.getProductId()));
            item.setQuantity(itemReq.getQuantity());
            return item;
        }).collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);
        order.setItems(items);

        Order saved = orderRepository.save(order);
        log.info("Order created: id={}, totalPrice={}, itemCount={}",
                saved.getId(), saved.getTotalPrice(), saved.getItems().size());
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }
}
