package com.pollinate.order.management.service;

import com.pollinate.order.management.dto.ProductRequest;
import com.pollinate.order.management.dto.ProductResponse;
import com.pollinate.order.management.exception.ResourceNotFoundException;
import com.pollinate.order.management.model.Product;
import com.pollinate.order.management.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }
}
