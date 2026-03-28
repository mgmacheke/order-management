package com.pollinate.order.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AUTH =
            "Basic " + Base64.getEncoder().encodeToString("admin:pass".getBytes());

    private ProductRequest sampleRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Google Pixel 8");
        request.setDescription("Quality smart phone");
        request.setPrice(new BigDecimal("13499.99"));
        return request;
    }

    @Test
    void createProduct_returns201WithBody() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Google Pixel 8"))
                .andExpect(jsonPath("$.price").value(13499.99));
    }

    @Test
    void createProduct_returns401WithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProductById_returnsProduct() throws Exception {
        // Create a product first
        MvcResult created = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andReturn();

        Long id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/api/v1/products/" + id)
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Google Pixel 8"));
    }

    @Test
    void getProductById_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/999999")
                        .header("Authorization", AUTH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listProducts_returnsPaginatedResult() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", AUTH)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
