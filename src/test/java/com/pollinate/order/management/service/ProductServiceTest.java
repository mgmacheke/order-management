package com.pollinate.order.management.service;

import com.pollinate.order.management.dto.ProductRequest;
import com.pollinate.order.management.dto.ProductResponse;
import com.pollinate.order.management.exception.ResourceNotFoundException;
import com.pollinate.order.management.model.Product;
import com.pollinate.order.management.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void create_savesProductAndReturnsResponse() {
        ProductRequest request = new ProductRequest();
        request.setName("Google Pixel 8");
        request.setDescription("Quality Phone");
        request.setPrice(new BigDecimal("134299.99"));

        Product saved = new Product();
        saved.setId(1L);
        saved.setName("Google Pixel 8");
        saved.setDescription("Quality Phone");
        saved.setPrice(new BigDecimal("134299.99"));

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.create(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Google Pixel 8");
        assertThat(response.getPrice()).isEqualByComparingTo("134299.99");
    }

    @Test
    void getById_returnsProductWhenFound() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Google Pixel 8");
        product.setPrice(new BigDecimal("134299.99"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Google Pixel 8");
    }

    @Test
    void getById_throwsResourceNotFoundWhenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_returnsPageOfProductResponses() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Google Pixel 8");
        product.setPrice(new BigDecimal("134299.99"));

        when(productRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = productService.findAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Google Pixel 8");
    }
}
