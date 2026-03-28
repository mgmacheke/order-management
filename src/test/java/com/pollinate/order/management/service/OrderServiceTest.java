package com.pollinate.order.management.service;

import com.pollinate.order.management.dto.OrderItemRequest;
import com.pollinate.order.management.dto.OrderRequest;
import com.pollinate.order.management.exception.OrderValidationException;
import com.pollinate.order.management.exception.ResourceNotFoundException;
import com.pollinate.order.management.model.Order;
import com.pollinate.order.management.model.Product;
import com.pollinate.order.management.repository.OrderRepository;
import com.pollinate.order.management.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void create_calculatesTotalPriceCorrectly() {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Phone cover");
        p1.setPrice(new BigDecimal("100.00"));

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Phone charger");
        p2.setPrice(new BigDecimal("50.00"));

        when(productRepository.findAllById(anyList())).thenReturn(List.of(p1, p2));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        Order returned = new Order();
        returned.setId(1L);
        returned.setStatus("CREATED");
        returned.setTotalPrice(new BigDecimal("250.00"));
        returned.setCreatedAt(LocalDateTime.now());
        returned.setItems(new ArrayList<>());
        when(orderRepository.save(captor.capture())).thenReturn(returned);

        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(2L);
        item2.setQuantity(1);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item1, item2));

        orderService.create(request);

        Order captured = captor.getValue();
        assertThat(captured.getTotalPrice()).isEqualByComparingTo("250.00");
        assertThat(captured.getStatus()).isEqualTo("CREATED");
        assertThat(captured.getItems()).hasSize(2);
    }

    @Test
    void create_rejectsOrderWhenAnyProductIdMissing() {
        when(productRepository.findAllById(anyList())).thenReturn(List.of());

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(99L);
        item.setQuantity(1);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item));

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(OrderValidationException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_rejectsOrderWhenSomeProductIdsMissing() {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setPrice(new BigDecimal("100.00"));

        when(productRepository.findAllById(anyList())).thenReturn(List.of(p1));

        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(1);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(2L);
        item2.setQuantity(1);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item1, item2));

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(OrderValidationException.class)
                .hasMessageContaining("2");
    }

    @Test
    void getById_throwsResourceNotFoundWhenMissing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_returnsPageOfOrderResponses() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("CREATED");
        order.setTotalPrice(new BigDecimal("100.00"));
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());

        when(orderRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(order)));

        var result = orderService.findAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("CREATED");
    }
}
