package com.pollinate.order.management.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pollinate.order.management.dto.OrderItemRequest;
import com.pollinate.order.management.dto.OrderRequest;
import com.pollinate.order.management.dto.ProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AUTH =
            "Basic " + Base64.getEncoder().encodeToString("admin:pass".getBytes());

    private Long createProduct(String name, double price) throws Exception {
        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setPrice(BigDecimal.valueOf(price));

        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    void createOrder_returns201WithCalculatedTotal() throws Exception {
        Long pid = createProduct("Phone cover", 100.00);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(pid);
        item.setQuantity(3);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalPrice").value(300.00))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void createOrder_returns400WhenProductIdMissing() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(999999L);
        item.setQuantity(1);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createOrder_returns401WithoutAuth() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setItems(List.of());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrderById_returnsOrder() throws Exception {
        Long pid = createProduct("Phone charger", 50.00);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(pid);
        item.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item));

        MvcResult created = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        Long orderId = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.totalPrice").value(100.00));
    }

    @Test
    void getOrderById_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/orders/999999")
                        .header("Authorization", AUTH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listOrders_returnsPaginatedResult() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", AUTH)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
