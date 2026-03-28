package com.pollinate.order.management.dto;

import com.pollinate.order.management.model.OrderItem;

public class OrderItemResponse {
    private Long productId;
    private String productName;
    private int quantity;

    public static OrderItemResponse from(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.productId = orderItem.getId();
        response.productName = response.getProductName();
        response.quantity = orderItem.getQuantity();
        return response;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
